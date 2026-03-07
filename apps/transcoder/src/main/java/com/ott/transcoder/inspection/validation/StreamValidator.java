package com.ott.transcoder.inspection.validation;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.fatal.UnsupportedMediaException;
import com.ott.transcoder.inspection.probe.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.ott.transcoder.constant.IngestJobConstant.DirectoryConstant.*;

/**
 * probe 후 스트림 수준 검증
 *
 * ffprobe 결과(ProbeResult)를 받아, 트랜스코딩을 진행해도 안전한지 판단
 *
 * 검증 항목:
 *   1. 비디오 스트림 존재 (오디오만 있는 파일 차단)
 *   2. 비디오 코덱 지원 여부
 *   3. duration 유효성 (0초, 비정상적으로 긴 영상)
 *   4. 해상도 범위 (너무 작거나 너무 큰 영상)
 *   5. 프레임레이트 이상 감지
 *   6. 손상 감지 (메타데이터 불완전)
 */
@Slf4j
@Component
public class StreamValidator {

    // FFmpeg이 디코딩 가능한 일반적인 비디오 코덱
    private static final Set<String> SUPPORTED_VIDEO_CODEC_SET = Set.of(
            "h264", "hevc", "h265", "vp8", "vp9", "av1",
            "mpeg4", "mpeg2video", "mpeg1video",
            "wmv3", "vc1",
            "theora", "prores", "dnxhd",
            "mjpeg", "rawvideo"
    );



    @Value("${transcoder.validation.max-duration-seconds:43200}")
    private double maxDurationSeconds; // 기본 12시간

    public void validate(ProbeResult probeResult) {
        // 1. 비디오 코덱 존재 및 지원 여부
        if (probeResult.videoCodec() == null || probeResult.videoCodec().isBlank()) {
            throw new UnsupportedMediaException(TranscodeErrorCode.NO_VIDEO_STREAM,
                    "비디오 코덱 정보 없음");
        }
        if (!SUPPORTED_VIDEO_CODEC_SET.contains(probeResult.videoCodec().toLowerCase())) {
            throw new UnsupportedMediaException(TranscodeErrorCode.UNSUPPORTED_CODEC,
                    "지원하지 않는 비디오 코덱 - codec: " + probeResult.videoCodec());
        }

        // 2. 해상도 범위
        if (probeResult.width() < MIN_RESOLUTION || probeResult.height() < MIN_RESOLUTION) {
            throw new UnsupportedMediaException(TranscodeErrorCode.INVALID_RESOLUTION,
                    "해상도가 너무 작음 - " + probeResult.width() + "x" + probeResult.height());
        }
        if (probeResult.width() > MAX_RESOLUTION || probeResult.height() > MAX_RESOLUTION) {
            throw new UnsupportedMediaException(TranscodeErrorCode.INVALID_RESOLUTION,
                    "해상도가 너무 큼 - " + probeResult.width() + "x" + probeResult.height());
        }

        // 3. duration 유효성
        if (probeResult.durationSeconds() <= 0) {
            throw new UnsupportedMediaException(TranscodeErrorCode.INVALID_DURATION,
                    "duration이 유효하지 않음 - " + probeResult.durationSeconds() + "s");
        }
        if (probeResult.durationSeconds() > maxDurationSeconds) {
            throw new UnsupportedMediaException(TranscodeErrorCode.INVALID_DURATION,
                    "duration 상한 초과 - " + probeResult.durationSeconds() + "s, max: " + maxDurationSeconds + "s");
        }

        // 4. 프레임레이트 이상
        if (probeResult.fps() <= 0) {
            throw new UnsupportedMediaException(TranscodeErrorCode.INVALID_FPS,
                    "프레임레이트가 유효하지 않음 - fps: " + probeResult.fps());
        }
        if (probeResult.fps() > MAX_FPS) {
            throw new UnsupportedMediaException(TranscodeErrorCode.INVALID_FPS,
                    "프레임레이트가 비정상적으로 높음 - fps: " + probeResult.fps() + ", max: " + MAX_FPS);
        }

        log.info("스트림 검증 통과 - {}x{}, duration: {}s, codec: {}, fps: {}",
                probeResult.width(), probeResult.height(),
                probeResult.durationSeconds(), probeResult.videoCodec(), probeResult.fps());
    }
}
