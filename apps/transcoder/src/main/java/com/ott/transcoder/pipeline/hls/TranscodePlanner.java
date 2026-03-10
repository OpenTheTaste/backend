package com.ott.transcoder.pipeline.hls;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.command.TranscodeCommand;
import com.ott.transcoder.ffmpeg.TranscodeProfile;
import com.ott.transcoder.inspection.probe.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.ott.transcoder.constant.IngestJobConstant.AudioConstant.AAC;
import static com.ott.transcoder.constant.IngestJobConstant.VideoConstant.LIBX264;
import static com.ott.transcoder.constant.IngestJobConstant.VideoConstant.PRESET_FAST;

/**
 * 단일 해상도에 대한 트랜스코딩 프로파일 결정
 * 원본 비트레이트 상한 적용, 코덱/프리셋 결정 등
 */
@Slf4j
@Component
public class TranscodePlanner {

    /**
     * 트랜스코드 커맨드 기반 트랜스코딩 프로파일 생성
     *
     * @param command     트랜스코드 커맨드 (해상도 포함)
     * @param probeResult ffprobe 결과
     * @return 해당 해상도의 트랜스코딩 프로파일 (비트레이트, 코덱, 프리셋 포함)
     */
    public TranscodeProfile plan(TranscodeCommand command, ProbeResult probeResult) {
        Resolution resolution = command.getResolution();
        String videoBitrate = decideVideoBitrate(probeResult, resolution);
        String audioBitrate = resolution.getAudioBitrate();
        String audioCodec = decideAudioCodec(probeResult);

        TranscodeProfile profile = new TranscodeProfile(
                resolution,
                resolution.getHeight(),
                videoBitrate,
                audioBitrate,
                LIBX264,
                audioCodec,
                PRESET_FAST
        );

        log.info("트랜스코딩 프로파일 결정 - resolution: {}, videoBitrate: {}, audioBitrate: {}",
                resolution.getKey(), videoBitrate, audioBitrate);

        return profile;
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
