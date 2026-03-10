package com.ott.transcoder.job;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.command.Command;
import com.ott.transcoder.command.CommandExtractor;
import com.ott.transcoder.command.CommandType;
import com.ott.transcoder.command.TranscodeCommand;
import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.StorageException;
import com.ott.transcoder.inspection.DiskSpaceGuard;
import com.ott.transcoder.inspection.Inspector;
import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.pipeline.CommandPipelineExecutor;
import com.ott.transcoder.pipeline.hls.MasterPlaylistGenerator;
import com.ott.transcoder.queue.TranscodeMessage;
import com.ott.transcoder.storage.VideoStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Value("${transcoder.ffmpeg.temp-dir:#{systemProperties['java.io.tmpdir'] + '/ott-transcode'}}")
    private String tempDir;

    /**
     * 트랜스코딩 작업 실행
     * 모든 예외는 밖으로 던져지며, RabbitConfig에 따라 재시도 여부 결정
     */
    public void handle(TranscodeMessage message) {
        Long mediaId = message.mediaId();
        Long ingestJobId = message.ingestJobId();
        Path workDir = Path.of(tempDir, PREFIX_WORK_DIR + mediaId + SUFFIX_WORK_DIR + ingestJobId);

        try {
            // 1. 작업 디렉토리 생성 (공간 체크를 위해 디렉토리가 존재해야 함)
            createWorkDir(workDir);

            // 2. 디스크 공간 확인 (메시지의 fileSize 기반)
            diskSpaceGuard.check(workDir, message.fileSize() != null ? message.fileSize() : 0L);

            Path outputDir = workDir.resolve("output");
            createWorkDir(outputDir);

            // 3. 원본 다운로드 (RetryableException 발생 가능)
            Path inputFile = videoStorage.download(message.originUrl(), workDir);

            // 4. 미디어 검사 (Fatal/RetryableException 발생 가능)
            ProbeResult probeResult = inspector.inspect(inputFile);

            // 5. 커맨드 추출
            List<Command> commandList = commandExtractor.extractCommand(message, probeResult);

            // 6. JobContext 생성
            JobContext jobContext = new JobContext(mediaId, ingestJobId, workDir, outputDir, inputFile, probeResult);

            // 7. 커맨드별 파이프라인 실행 (MAIN)
            for (Command command : commandList)
                commandPipelineExecutor.execute(command, jobContext);

            // === POST ===

            // 8. 마스터 플레이리스트 생성
            List<Resolution> resolutionList = commandList.stream()
                    .filter(c -> c.getType() == CommandType.TRANSCODE)
                    .map(c -> ((TranscodeCommand) c).getResolution())
                    .toList();
            masterPlaylistGenerator.generate(outputDir, resolutionList);

            // 9. 결과물 업로드 (outputDir만 — 원본 제외)
            String uploadedPath = videoStorage.upload(outputDir, "media/" + mediaId + "/hls");

            log.info("모든 작업 성공 - mediaId: {}, ingestJobId: {}, uploadedPath: {}",
                    mediaId, ingestJobId, uploadedPath);

        } finally {
            // 예외 발생 여부와 상관없이 로컬 작업 디렉토리는 반드시 정리합니다.
            cleanUp(workDir);
        }
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
