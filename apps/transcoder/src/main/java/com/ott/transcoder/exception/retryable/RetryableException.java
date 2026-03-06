package com.ott.transcoder.exception.retryable;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.TranscodeException;

/** 재시도 가능 — RabbitMQ requeue 대상 */
public class RetryableException extends TranscodeException {
    public RetryableException(TranscodeErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public RetryableException(TranscodeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
