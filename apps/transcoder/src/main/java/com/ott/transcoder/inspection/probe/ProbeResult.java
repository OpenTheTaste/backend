package com.ott.transcoder.inspection.probe;

/**
 * ffprobe 실행 결과를 담는 불변 레코드
 *
 * @param width          영상 너비 (px)
 * @param height         영상 높이 (px)
 * @param durationSeconds 전체 재생 시간 (초)
 * @param videoCodec     비디오 코덱 (예: h264, hevc, vp9)
 * @param audioCodec     오디오 코덱 (예: aac, opus, "none")
 * @param fps            프레임레이트
 * @param videoBitrate   비디오 비트레이트 (bps)
 * @param audioBitrate   오디오 비트레이트 (bps)
 * @param audioChannels  오디오 채널 수 (예: 2=stereo, 6=5.1ch)
 * @param pixelFormat    픽셀 포맷 (예: yuv420p, yuv422p)
 * @param rotation       회전 각도 (0, 90, 180, 270). 스마트폰 세로 촬영 시 90 또는 270
 */
public record ProbeResult(
        int width,
        int height,
        double durationSeconds,
        String videoCodec,
        String audioCodec,
        double fps,
        long videoBitrate,
        long audioBitrate,
        int audioChannels,
        String pixelFormat,
        int rotation
) {
    /**
     * 회전을 고려한 실제 영상 높이.
     * 90° 또는 270° 회전된 영상은 width와 height가 뒤바뀐다.
     * 예: 1080x1920(세로 촬영, rotation=90) → 실제 출력은 1920x1080 → effectiveHeight = 1080
     */
    public int effectiveHeight() {
        return isRotated() ? this.width : this.height;
    }

    public int effectiveWidth() {
        return isRotated() ? this.height : this.width;
    }

    public boolean isRotated() {
        return rotation == 90 || rotation == 270;
    }

    // 회전을 고려하여 업스케일 여부 판단
    public boolean isUpscaleFor(int targetHeight) {
        return targetHeight > effectiveHeight();
    }
}
