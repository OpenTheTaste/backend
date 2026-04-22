package com.ott.transcoder.job;

import com.ott.domain.ingest_command.domain.CommandType;
import com.ott.transcoder.ffmpeg.Resolution;
import com.ott.transcoder.command.Command;
import com.ott.transcoder.command.CommandExtractor;
import com.ott.transcoder.command.TranscodeCommand;
import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.StorageException;
import com.ott.transcoder.heartbeat.Heartbeat;
import com.ott.transcoder.heartbeat.HeartbeatScheduler;
import com.ott.transcoder.inspection.DiskSpaceGuard;
import com.ott.transcoder.inspection.Inspector;
import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.pipeline.CommandPipelineExecutor;
import com.ott.transcoder.pipeline.hls.MasterPlaylistGenerator;
import com.ott.infra.mq.TranscodeMessage;
import com.ott.transcoder.queue.rabbit.DelayQueuePublisher;
import com.ott.transcoder.storage.VideoStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.ott.transcoder.constant.IngestJobConstant.DirectoryConstant.PREFIX_WORK_DIR;
import static com.ott.transcoder.constant.IngestJobConstant.DirectoryConstant.SUFFIX_WORK_DIR;

/**
 * 작업 전체 흐름 조율
 * 인프라(RabbitMQ)에서 예외 처리를 전담하므로, 여기서는 핵심 비즈니스 로직과 자원 정리(Cleanup)에만 집중합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JobOrchestrator {

    private final DiskSpaceGuard diskSpaceGuard;
    private final VideoStorage videoStorage;
    private final Inspector inspector;

    private final CommandExtractor commandExtractor;
    private final CommandPipelineExecutor commandPipelineExecutor;
    private final MasterPlaylistGenerator masterPlaylistGenerator;

    private final IngestJobStatusManager statusManager;
    private final HeartbeatScheduler heartbeatScheduler;
    private final DelayQueuePublisher delayQueuePublisher;

    @Value("${transcoder.ffmpeg.temp-dir:#{systemProperties['java.io.tmpdir'] + '/ott-transcode'}}")
    private String tempDir;

    /**
     * 메시지 수신 진입점 — 4계층 방어 분기
     * 모든 예외는 밖으로 던져지며, RabbitConfig에 따라 재시도 여부 결정
     */
    public void handle(TranscodeMessage message, boolean delayed) {
        Long ingestJobId = message.ingestJobId();

        // ── Layer 1: 종료 상태 조기 탈출 (최적화용, CAS만으로도 정확성 보장됨) ──
        if (statusManager.isTerminal(ingestJobId)) {
            log.info("종료 상태 IngestJob 재수신 - ingestJobId: {} (ACK skip)", ingestJobId);
            return;
        }

        // ── Layer 2: CAS 선점 시도 ──
        if (statusManager.startProcessing(ingestJobId)) {
            executeTranscoding(message);
            return;
        }

        // ── CAS 실패: Layer 3 or 4 ──
        if (!delayed) {
            // Layer 3: 최초 수신 → Delay Queue로 1회 발행
            log.info("CAS 실패 (점유 중) → Delay Queue 발행 - ingestJobId: {}", ingestJobId);
            delayQueuePublisher.publishToDelay(message);
        } else {
            // Layer 4: Delay 복귀인데 여전히 점유 중 → 포기
            log.info("CAS 실패 (Delay 복귀 후에도 점유 중) → ACK drop - ingestJobId: {}", ingestJobId);
        }
    }

    /**
     * 트랜스코딩 실행 (CAS 선점 성공 후 호출)
     */
    private void executeTranscoding(TranscodeMessage message) {
        Long mediaId = message.mediaId();
        Long ingestJobId = message.ingestJobId();
        Path workDir = Path.of(tempDir, PREFIX_WORK_DIR + mediaId + SUFFIX_WORK_DIR + ingestJobId);

        try (Heartbeat heartbeat = heartbeatScheduler.start(ingestJobId)) {
            // 1. 작업 디렉토리 생성 (공간 체크를 위해 디렉토리가 존재해야 함)
            createWorkDir(workDir);

            // 2. 디스크 공간 확인
            diskSpaceGuard.check(workDir, message.fileSize() != null ? message.fileSize() : 0L);

            Path outputDir = workDir.resolve("output");
            createWorkDir(outputDir);

            // 3. 원본 다운로드
            Path inputFile = videoStorage.download(message.originUrl(), workDir);

            // 4. 미디어 검사
            ProbeResult probeResult = inspector.inspect(inputFile);

            // 5. 커맨드 추출 + 완료 필터링 + DB 저장
            List<Command> commandList = commandExtractor.extractCommand(message, probeResult);

            // 6. JobContext 생성
            String uploadPrefix = resolveUploadPrefix(message.originUrl());
            JobContext jobContext = new JobContext(
                    mediaId, ingestJobId, workDir, outputDir, inputFile, probeResult, uploadPrefix
            );

            // 재시도 시 이미 완료된 해상도 복원 (master.m3u8용)
            List<Resolution> completedResolutionList = new ArrayList<>(
                    statusManager.getCompletedResolutions(ingestJobId)
            );

            // 7. 커맨드별 파이프라인 실행 (처리 + 업로드) + 상태 반영
            for (Command command : commandList) {
                String outputUrl = commandPipelineExecutor.execute(command, jobContext);

                if (command.getType() == CommandType.TRANSCODE) {
                    TranscodeCommand tc = (TranscodeCommand) command;

                    // master.m3u8 점진적 갱신 + S3 업로드 (cross-command)
                    completedResolutionList.add(tc.getResolution());
                    masterPlaylistGenerator.generate(outputDir, completedResolutionList);
                    videoStorage.putFile(outputDir.resolve("master.m3u8"), uploadPrefix + "/master.m3u8");

                    // CP-5: DB 반영 (outputUrl + 최초 시 미디어 활성화)
                    statusManager.completeTranscodeCommand(ingestJobId, command, outputUrl);
                } else {
                    // CP-5: 비-트랜스코드 커맨드
                    statusManager.completeCommand(ingestJobId, command, outputUrl);
                }
            }

            // CP-6: 전체 완료 확인
            statusManager.checkAllCompleted(ingestJobId);

            log.info("모든 작업 성공 - mediaId: {}, ingestJobId: {}", mediaId, ingestJobId);

        } finally {
            cleanUp(workDir);
        }
    }

    private String resolveUploadPrefix(String originUrl) {
        int originIndex = originUrl.indexOf("/origin/");
        if (originIndex == -1) {
            throw new IllegalStateException("originUrl에서 /origin/ 경로를 찾을 수 없음: " + originUrl);
        }
        return originUrl.substring(0, originIndex) + "/transcoded";
    }

    private void createWorkDir(Path workDir) {
        try {
            Files.createDirectories(workDir);
        } catch (IOException e) {
            throw new StorageException(TranscodeErrorCode.STORAGE_FAILED,
                    "작업 디렉토리 생성 실패 - " + workDir, e);
        }
    }

    private void cleanUp(Path workDir) {
        try {
            if (Files.exists(workDir)) {
                Files.walk(workDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // 삭제 실패는 로그만 남기고 무시 (작업 성공 여부에 지장 없음)
                            }
                        });
                log.info("작업 디렉토리 정리 완료 - {}", workDir);
            }
        } catch (IOException e) {
            log.warn("작업 디렉토리 정리 중 오류 발생 (무시함) - {}", workDir, e);
        }
    }
}
