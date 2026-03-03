package com.ott.transcoder.ffmpeg;

import com.ott.domain.video_profile.domain.Resolution;

/**
 * 단일 해상도에 대한 트랜스코딩 설정 묶음
 *
 * 현재는 Resolution enum 기반의 고정 프리셋이지만,
 * 향후 TranscodePlanner가 ProbeResult를 분석하여 동적으로 생성
 *
 * @param resolution    대상 해상도 (DB 저장용)
 * @param height        출력 높이 (px). 너비는 FFmpeg -2 옵션으로 자동 계산
 * @param videoBitrate  비디오 비트레이트 (예: "800k", "2400k")
 * @param audioBitrate  오디오 비트레이트 (예: "96k", "128k")
 * @param videoCodec    비디오 인코더 (예: "libx264")
 * @param audioCodec    오디오 인코더 (예: "aac")
 * @param preset        인코딩 프리셋 (예: "fast", "medium")
 */
public record TranscodeProfile(
        Resolution resolution,
        int height,
        String videoBitrate,
        String audioBitrate,
        String videoCodec,
        String audioCodec,
        String preset
) {
    /** 기존 하드코딩 값과 동일한 기본 프리셋 */
    public static TranscodeProfile defaultFor(Resolution resolution) {
        return switch (resolution) {
            case P360 -> new TranscodeProfile(resolution, 360, "800k", "96k", "libx264", "aac", "fast");
            case P720 -> new TranscodeProfile(resolution, 720, "2400k", "128k", "libx264", "aac", "fast");
            case P1080 -> new TranscodeProfile(resolution, 1080, "4800k", "192k", "libx264", "aac", "fast");
        };
    }
}
