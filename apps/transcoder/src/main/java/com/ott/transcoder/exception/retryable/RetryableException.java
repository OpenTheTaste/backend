package com.ott.transcoder.exception.retryable;

import com.ott.transcoder.exception.TranscodeException;

// 일시적인 오류로 인해 재시도가 필요한 경우 발생하는 예외
public class RetryableException extends TranscodeException {
    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
