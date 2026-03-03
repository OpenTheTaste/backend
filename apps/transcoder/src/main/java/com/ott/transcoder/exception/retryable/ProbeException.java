package com.ott.transcoder.exception.retryable;

/**
 * 미디어 정보 분석(ffprobe) 중 발생하는 예외
 */
public class ProbeException extends RetryableException {
    public ProbeException(String message) {
        super(message);
    }

    public ProbeException(String message, Throwable cause) {
        super(message, cause);
    }
}
