package com.ott.transcoder.pipeline.thumbnail;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.FfmpegException;
import com.ott.transcoder.inspection.probe.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 영상에서 대표 썸네일 1장을 추출
 *
 * 후보 시점(10%, 25%, 33%)에서 프레임을 추출한 뒤,
 * 평균 밝기(YAVG)를 측정하여 검정 프레임을 회피한다.
 * 모든 후보가 임계값 이하이면 가장 밝은 프레임을 채택한다.
 */
@Slf4j
@Component
public class ThumbnailExtractor {

    private static final double[] CANDIDATE_RATIOS = {0.10, 0.25, 0.33};
    private static final double MIN_SEEK_SECONDS = 2.0;
    private static final Pattern YAVG_PATTERN = Pattern.compile("lavfi\\.signalstats\\.YAVG=(\\d+\\.?\\d*)");

    @Value("${transcoder.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${transcoder.thumbnail.brightness-threshold:30}")
    private double brightnessThreshold;

    @Value("${transcoder.thumbnail.height:720}")
    private int thumbnailHeight;

    @Value("${transcoder.thumbnail.quality:2}")
    private int jpegQuality;

    /**
     * 대표 썸네일을 추출하여 outputDir/thumbnail.jpg로 저장
     *
     * @param inputFile   원본 영상 파일
     * @param outputDir   썸네일 저장 디렉토리
     * @param probeResult ffprobe 결과 (duration 사용)
     * @return 생성된 썸네일 파일 경로
     */
    public Path extract(Path inputFile, Path outputDir, ProbeResult probeResult) {

        double duration = probeResult.durationSeconds();
        Path thumbnailDir = outputDir.resolve("thumbnail");

        try {
            Files.createDirectories(thumbnailDir);
        } catch (IOException e) {
            throw new FfmpegException(TranscodeErrorCode.FFMPEG_FAILED,
                    "썸네일 디렉토리 생성 실패", e);
        }

        Path bestCandidate = null;
        double bestBrightness = -1;

        for (int i = 0; i < CANDIDATE_RATIOS.length; i++) {
            double seekSeconds = Math.max(duration * CANDIDATE_RATIOS[i], MIN_SEEK_SECONDS);
            // duration보다 크면 마지막 프레임 근처로 보정
            if (seekSeconds >= duration) {
                seekSeconds = Math.max(duration - 1.0, 0);
            }

            Path candidateFile = thumbnailDir.resolve("candidate_" + i + ".jpg");

            extractFrame(inputFile, candidateFile, seekSeconds);

            if (!Files.exists(candidateFile)) {
                log.warn("프레임 추출 실패 - seekSeconds: {}", seekSeconds);
                continue;
            }

            double brightness = measureBrightness(inputFile, seekSeconds);
            log.info("썸네일 후보 - index: {}, seekSeconds: {}s, brightness: {}",
                    i, String.format("%.1f", seekSeconds), String.format("%.1f", brightness));

            if (brightness > bestBrightness) {
                bestBrightness = brightness;
                bestCandidate = candidateFile;
            }

            // 임계값 통과 시 즉시 채택
            if (brightness >= brightnessThreshold) {
                log.info("밝기 임계값 통과 - index: {}, brightness: {} >= threshold: {}",
                        i, String.format("%.1f", brightness), brightnessThreshold);
                break;
            }

            log.info("밝기 임계값 미달 - index: {}, brightness: {} < threshold: {}",
                    i, String.format("%.1f", brightness), brightnessThreshold);
        }

        if (bestCandidate == null) {
            throw new FfmpegException(TranscodeErrorCode.FFMPEG_FAILED,
                    "모든 후보 프레임 추출 실패");
        }

        // 최종 썸네일 파일로 이동
        Path finalThumbnail = thumbnailDir.resolve("thumbnail.jpg");
        try {
            Files.move(bestCandidate, finalThumbnail, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FfmpegException(TranscodeErrorCode.FFMPEG_FAILED,
                    "썸네일 파일 이동 실패", e);
        }

        // 나머지 후보 파일 정리
        cleanupCandidates(thumbnailDir);

        log.info("썸네일 추출 완료 - path: {}, brightness: {}", finalThumbnail, String.format("%.1f", bestBrightness));
        return finalThumbnail;
    }

    /**
     * 특정 시점의 프레임을 JPEG으로 추출
     */
    private void extractFrame(Path inputFile, Path outputFile, double seekSeconds) {
        List<String> command = List.of(
                ffmpegPath,
                "-ss", String.format("%.3f", seekSeconds),
                "-i", inputFile.toString(),
                "-vframes", "1",
                "-vf", "scale=-2:" + thumbnailHeight,
                "-q:v", String.valueOf(jpegQuality),
                "-y",
                outputFile.toString()
        );

        executeProcess(command, "프레임 추출");
    }

    /**
     * 특정 시점 프레임의 평균 밝기(YAVG) 측정
     * signalstats 필터로 Luma 평균값을 구한다 (0~255, 낮을수록 어두움)
     */
    private double measureBrightness(Path inputFile, double seekSeconds) {
        List<String> command = List.of(
                ffmpegPath,
                "-ss", String.format("%.3f", seekSeconds),
                "-i", inputFile.toString(),
                "-vframes", "1",
                "-vf", "signalstats",
                "-f", "null",
                "-"
        );

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
            }

            boolean finished = process.waitFor(1, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                log.warn("밝기 측정 타임아웃 - seekSeconds: {}", seekSeconds);
                return 0;
            }

            Matcher matcher = YAVG_PATTERN.matcher(output);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }

            log.warn("YAVG 파싱 실패 - seekSeconds: {}", seekSeconds);
            return 0;

        } catch (IOException | InterruptedException e) {
            log.warn("밝기 측정 실패 - seekSeconds: {}", seekSeconds, e);
            return 0;
        }
    }

    private void executeProcess(List<String> command, String label) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 출력 소비 (블로킹 방지)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                    // consume
                }
            }

            boolean finished = process.waitFor(2, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new FfmpegException(TranscodeErrorCode.FFMPEG_TIMEOUT,
                        label + " 타임아웃");
            }
            if (process.exitValue() != 0) {
                throw new FfmpegException(TranscodeErrorCode.FFMPEG_FAILED,
                        label + " 실패 - exitCode: " + process.exitValue());
            }
        } catch (IOException | InterruptedException e) {
            throw new FfmpegException(TranscodeErrorCode.FFMPEG_FAILED,
                    label + " 실행 실패", e);
        }
    }

    private void cleanupCandidates(Path thumbnailDir) {
        try {
            Files.list(thumbnailDir)
                    .filter(p -> p.getFileName().toString().startsWith("candidate_"))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {
        }
    }
}