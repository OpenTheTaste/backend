package com.ott.transcoder.pipeline.hls;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.ffmpeg.TranscodeProfile;
import com.ott.transcoder.inspection.probe.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.ott.transcoder.constant.IngestJobConstant.AudioConstant.AAC;
import static com.ott.transcoder.constant.IngestJobConstant.VideoConstant.LIBX264;
import static com.ott.transcoder.constant.IngestJobConstant.VideoConstant.PRESET_FAST;

/**
 * ProbeResult를 분석하여 HLS 트랜스코딩 대상 해상도/비트레이트 결정
 * 업스케일 방지, 원본 비트레이트 상한 적용 등
 */
@Slf4j
@Component
public class TranscodePlanner {

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
            int targetHeight = resolution.getHeight();

            // 업스케일 방지
            if (probeResult.isUpscaleFor(targetHeight)) {
                continue;
            }

            String videoBitrate = decideVideoBitrate(probeResult, resolution);
            String audioBitrate = resolution.getAudioBitrate();
            String audioCodec = decideAudioCodec(probeResult);

            TranscodeProfile profile = new TranscodeProfile(
                    resolution,
                    targetHeight,
                    videoBitrate,
                    audioBitrate,
                    LIBX264,
                    audioCodec,
                    PRESET_FAST
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
        long defaultBitrate = resolution.getVideoBitrate();
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
        return AAC;
    }

    private String formatBitrate(long bps) {
        if (bps >= 1_000_000) {
            return (bps / 1_000) + "k";
        }
        return bps + "";
    }
}
