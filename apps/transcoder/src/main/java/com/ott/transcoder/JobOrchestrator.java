package com.ott.transcoder;

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
 * diskSpaceGuard → workDir 생성 → download → inspect → pipeline 실행 → cleanup
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

    public void handle(TranscodeMessage message) throws Exception {
        Long mediaId = message.mediaId();
        // TODO: 0. DB 확인 필요

        Path workDir = Path.of(tempDir, "media-" + mediaId);

        // 1. 디스크 공간 확인
        diskSpaceGuard.check(Path.of(message.originUrl()));

        try {
            // 2. workDir 생성
            Files.createDirectories(workDir);

            // 3. 원본 다운로드
            Path inputFile = videoStorage.download(message.originUrl(), workDir);

            // 4. 검사 (FileValidator → Probe → StreamValidator)
            ProbeResult probeResult = inspector.inspect(inputFile);

            // TODO: 5. 커맨드 생성 -> 각 커맨드 파이프라인 실행

            // 6. 파이프라인 실행
            pipeline.execute(mediaId, inputFile, workDir, probeResult);

        } finally {
            cleanUp(workDir);
        }
    }

    private void cleanUp(Path workDir) {
        try {
            if (Files.exists(workDir)) {
                Files.walk(workDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                        });
                log.info("작업 디렉토리 정리 완료 - {}", workDir);
            }
        } catch (IOException e) {
            log.warn("작업 디렉토리 정리 실패 - {}", workDir, e);
        }
    }
}
