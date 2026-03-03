package com.ott.transcoder.exception;

/** 트랜스코더 모듈의 최상위 예외 클래스 */
public class TranscodeException extends RuntimeException {
    public TranscodeException(String message) {
        super(message);
    }

    public TranscodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
