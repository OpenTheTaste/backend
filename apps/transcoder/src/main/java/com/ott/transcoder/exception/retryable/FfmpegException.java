package com.ott.transcoder.exception.retryable;

/**
 * FFmpeg 실행 중 프로세스 오류나 타임아웃 발생 시 던지는 예외
 */
public class FfmpegException extends RetryableException {
    public FfmpegException(String message) {
        super(message);
    }

    public FfmpegException(String message, Throwable cause) {
        super(message, cause);
    }
}
