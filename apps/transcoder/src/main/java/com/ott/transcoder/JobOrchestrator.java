package com.ott.transcoder;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.StorageException;
import com.ott.transcoder.inspection.Inspector;
import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.inspection.DiskSpaceGuard;
import com.ott.transcoder.pipeline.CommandPipeline;
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
    private final CommandPipeline pipeline;

    @Value("${transcoder.ffmpeg.temp-dir:#{systemProperties['java.io.tmpdir'] + '/ott-transcode'}}")
    private String tempDir;

    /**
     * 트랜스코딩 작업 실행
     * 모든 예외는 밖으로 던져지며, RabbitConfig에 따라 재시도 여부 결정
     */
    public void handle(TranscodeMessage message) {
        Long mediaId = message.mediaId();
        Path workDir = Path.of(tempDir, "media-" + mediaId);

        try {
            // 1. 디스크 공간 확인
            diskSpaceGuard.check(Path.of(message.originUrl()));

            // 2. 작업 디렉토리 생성
            createWorkDir(workDir);

            // 3. 원본 다운로드 (RetryableException 발생 가능)
            Path inputFile = videoStorage.download(message.originUrl(), workDir);

            // 4. 미디어 검사 (Fatal/RetryableException 발생 가능)
            ProbeResult probeResult = inspector.inspect(inputFile);

            // 5. 트랜스코딩 파이프라인 실행
            pipeline.execute(mediaId, inputFile, workDir, probeResult);

            log.info("모든 트랜스코딩 작업 성공 - mediaId: {}", mediaId);

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
