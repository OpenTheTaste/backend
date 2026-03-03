package com.ott.transcoder.ffmpeg.execution.processbuilder;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.FfmpegException;
import com.ott.transcoder.ffmpeg.execution.FfmpegExecutor;
import com.ott.transcoder.ffmpeg.TranscodeProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ProcessBuilder 기반 FFmpeg CLI 래퍼
 * 단일 해상도에 대해 HLS 트랜스코딩을 수행
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "transcoder.ffmpeg.engine", havingValue = "processbuilder")
public class ProcessBuilderFfmpegExecutor implements FfmpegExecutor {

    @Value("${transcoder.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${transcoder.ffmpeg.segment-duration:10}")
    private int segmentDuration;

    @Override
    public Path execute(Path inputFile, Path outputDir, TranscodeProfile profile) {
        String resolutionKey = profile.resolution().getKey().toLowerCase();

        try {
            // 해상도별 하위 디렉토리 생성 (예: workDir/360p/)
            Path resolutionDir = outputDir.resolve(resolutionKey);
            Files.createDirectories(resolutionDir);

            Path playlistPath = resolutionDir.resolve("media.m3u8");
            String segmentPattern = resolutionDir.resolve("segment_%03d.ts").toString();

            // FFmpeg 명령어 조립 — TranscodeProfile에서 설정값을 가져옴
            // TODO: FFmpeg Filter Chain 구성 로직 추가 필요
            List<String> command = List.of(
                    ffmpegPath, "-i", inputFile.toString(),
                    "-vf", "scale=-2:" + profile.height(),
                    "-c:v", profile.videoCodec(), "-preset", profile.preset(),
                    "-c:a", profile.audioCodec(), "-b:a", profile.audioBitrate(),
                    "-b:v", profile.videoBitrate(),
                    "-f", "hls",
                    "-hls_time", String.valueOf(segmentDuration),
                    "-hls_list_size", "0",
                    "-hls_segment_filename", segmentPattern,
                    playlistPath.toString()
            );

            log.info("FFmpeg 실행 - resolution: {}, command: {}", resolutionKey, String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line);
                }
            }

            boolean finished = process.waitFor(30, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new FfmpegException(TranscodeErrorCode.FFMPEG_TIMEOUT,
                        "FFmpeg 타임아웃 - resolution: " + resolutionKey);
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new FfmpegException(TranscodeErrorCode.FFMPEG_FAILED,
                        "FFmpeg 실패 - resolution: " + resolutionKey + ", exitCode: " + exitCode);
            }

            log.info("FFmpeg 완료 - resolution: {}, output: {}", resolutionKey, playlistPath);
            return playlistPath;

        } catch (IOException | InterruptedException e) {
            throw new FfmpegException(TranscodeErrorCode.FFMPEG_FAILED,
                    "FFmpeg 실행 실패 - resolution: " + resolutionKey, e);
        }
    }
}
