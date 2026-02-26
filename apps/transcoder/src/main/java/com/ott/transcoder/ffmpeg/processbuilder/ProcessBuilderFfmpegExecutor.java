package com.ott.transcoder.ffmpeg.processbuilder;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.ffmpeg.FfmpegExecutor;
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
import java.util.Map;

/**
 * ProcessBuilder 기반 FFmpeg CLI 래퍼
 *
 * 시스템에 설치된 FFmpeg 바이너리를 ProcessBuilder로 직접 호출
 * 단일 해상도에 대해 HLS 트랜스코딩을 수행하며,
 * 결과물로 media.m3u8 (미디어 플레이리스트) + segment_XXX.ts (세그먼트 파일)를 생성
 *
 * FFmpeg 내부 처리 흐름:
 *   Demux(컨테이너 분리) → Decode(디코딩) → Filter(스케일링) → Encode(재인코딩) → Mux(HLS 패키징)
 *   이 전체가 하나의 FFmpeg 명령어로 실행됨
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "transcoder.ffmpeg.engine", havingValue = "processbuilder")
public class ProcessBuilderFfmpegExecutor implements FfmpegExecutor {

    /** 해상도별 출력 높이 (너비는 -2로 자동 계산, 짝수 보장) */
    private static final Map<Resolution, Integer> HEIGHT_MAP = Map.of(
            Resolution.P360, 360,
            Resolution.P720, 720,
            Resolution.P1080, 1080
    );

    /** 해상도별 비디오 비트레이트 */
    private static final Map<Resolution, String> VIDEO_BITRATE_MAP = Map.of(
            Resolution.P360, "800k",
            Resolution.P720, "2400k",
            Resolution.P1080, "4800k"
    );

    /** 해상도별 오디오 비트레이트 */
    private static final Map<Resolution, String> AUDIO_BITRATE_MAP = Map.of(
            Resolution.P360, "96k",
            Resolution.P720, "128k",
            Resolution.P1080, "192k"
    );

    @Value("${transcoder.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    /** HLS 세그먼트 하나의 길이 (초) */
    @Value("${transcoder.ffmpeg.segment-duration:10}")
    private int segmentDuration;

    @Override
    public Path execute(Path inputFile, Path outputDir, Resolution resolution) throws IOException, InterruptedException {
        int height = HEIGHT_MAP.get(resolution);
        String videoBitrate = VIDEO_BITRATE_MAP.get(resolution);
        String audioBitrate = AUDIO_BITRATE_MAP.get(resolution);

        // 해상도별 하위 디렉토리 생성 (예: workDir/360p/)
        Path resolutionDir = outputDir.resolve(resolution.getKey().toLowerCase());
        Files.createDirectories(resolutionDir);

        Path playlistPath = resolutionDir.resolve("media.m3u8");
        String segmentPattern = resolutionDir.resolve("segment_%03d.ts").toString();

        // FFmpeg 명령어 조립
        List<String> command = List.of(
                ffmpegPath, "-i", inputFile.toString(),
                "-vf", "scale=-2:" + height,
                "-c:v", "libx264", "-preset", "fast",
                "-c:a", "aac", "-b:a", audioBitrate,
                "-b:v", videoBitrate,
                "-f", "hls",
                "-hls_time", String.valueOf(segmentDuration),
                "-hls_list_size", "0",
                "-hls_segment_filename", segmentPattern,
                playlistPath.toString()
        );

        log.info("FFmpeg 실행 - resolution: {}, command: {}", resolution.getKey(), String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // FFmpeg 출력을 읽어야 프로세스가 블로킹되지 않는다 (버퍼 가득 참 방지)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[FFmpeg] {}", line);
            }
        }

        boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("FFmpeg 타임아웃 - resolution: " + resolution.getKey());
        }
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 실패 - resolution: " + resolution.getKey() + ", exitCode: " + exitCode);
        }

        log.info("FFmpeg 완료 - resolution: {}, output: {}", resolution.getKey(), playlistPath);
        return playlistPath;
    }
}
