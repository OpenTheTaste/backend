package com.ott.transcoder.exception;

import lombok.Getter;

/** 트랜스코더 모듈의 최상위 예외 클래스 */
@Getter
public class TranscodeException extends RuntimeException {

    private final TranscodeErrorCode errorCode;

    public TranscodeException(TranscodeErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TranscodeException(TranscodeErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
