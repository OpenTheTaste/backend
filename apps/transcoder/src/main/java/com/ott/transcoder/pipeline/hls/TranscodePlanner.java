package com.ott.transcoder.pipeline.hls;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.ffmpeg.TranscodeProfile;
import com.ott.transcoder.inspection.probe.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProbeResult를 분석하여 HLS 트랜스코딩 대상 해상도/비트레이트 결정
 * 업스케일 방지, 원본 비트레이트 상한 적용 등
 */
@Slf4j
@Component
public class TranscodePlanner {

    /** 해상도별 높이 (Resolution enum에 height 필드가 없으므로 여기서 관리) */
    private static final Map<Resolution, Integer> HEIGHT_MAP = Map.of(
            Resolution.P360, 360,
            Resolution.P720, 720,
            Resolution.P1080, 1080
    );

    /** 해상도별 기본 비디오 비트레이트 (bps) — 원본 비트레이트와 비교용 */
    private static final Map<Resolution, Long> DEFAULT_VIDEO_BITRATE_MAP = Map.of(
            Resolution.P360, 800_000L,
            Resolution.P720, 2_400_000L,
            Resolution.P1080, 4_800_000L
    );

    /** 해상도별 기본 오디오 비트레이트 문자열 */
    private static final Map<Resolution, String> AUDIO_BITRATE_MAP = Map.of(
            Resolution.P360, "96k",
            Resolution.P720, "128k",
            Resolution.P1080, "192k"
    );

    /** 트랜스코딩 대상 해상도 */
    private static final List<Resolution> CANDIDATE_RESOLUTION_LIST = List.of(
            Resolution.P360, Resolution.P720, Resolution.P1080
    );

    /**
     * ProbeResult를 분석하여 트랜스코딩할 프로파일 목록 생성
     *
     * @param probeResult ffprobe 결과
     * @return 트랜스코딩 대상 프로파일 목록 (업스케일 해상도 제외)
     */
    public List<TranscodeProfile> plan(ProbeResult probeResult) {
        List<TranscodeProfile> profileList = new ArrayList<>();

        for (Resolution resolution : CANDIDATE_RESOLUTION_LIST) {
            int targetHeight = HEIGHT_MAP.get(resolution);

            // 업스케일 방지
            if (probeResult.isUpscaleFor(targetHeight)) {
                continue;
            }

            String videoBitrate = decideVideoBitrate(probeResult, resolution);
            String audioBitrate = AUDIO_BITRATE_MAP.get(resolution);
            String audioCodec = decideAudioCodec(probeResult);

            TranscodeProfile profile = new TranscodeProfile(
                    resolution,
                    targetHeight,
                    videoBitrate,
                    audioBitrate,
                    "libx264",
                    audioCodec,
                    "fast"
            );

            profileList.add(profile);
        }

        if (profileList.isEmpty()) {
            // 원본이 360p 미만이어도 최소 1개는 생성 (원본 해상도로)
            log.warn("모든 해상도가 업스케일 — 최소 프로파일 생성 (360p 기준, 원본 높이: {})", probeResult.height());
            profileList.add(TranscodeProfile.defaultFor(Resolution.P360));
        }

        log.info("트랜스코딩 계획 수립 완료 - 대상 해상도: {}",
                profileList.stream().map(p -> p.resolution().getKey()).toList());

        return profileList;
    }

    /**
     * 비디오 비트레이트 결정
     * 원본 비트레이트가 기본값보다 낮으면 원본 비트레이트를 상한으로 사용하여 과도한 할당을 방지
     */
    private String decideVideoBitrate(ProbeResult probeResult, Resolution resolution) {
        long defaultBitrate = DEFAULT_VIDEO_BITRATE_MAP.get(resolution);
        long originBitrate = probeResult.videoBitrate();

        // 원본 비트레이트 정보가 없으면 기본값 사용
        if (originBitrate <= 0) {
            return formatBitrate(defaultBitrate);
        }

        // 원본이 기본값보다 낮으면 원본을 상한으로
        long chosen = Math.min(defaultBitrate, originBitrate);
        return formatBitrate(chosen);
    }

    private String decideAudioCodec(ProbeResult probeResult) {
        return "aac";
    }

    private String formatBitrate(long bps) {
        if (bps >= 1_000_000) {
            return (bps / 1_000) + "k";
        }
        return bps + "";
    }
}
