package com.ott.transcoder.exception.fatal;

/**
 * 지원하지 않는 코덱이나 미디어 형식일 때 발생하는 예외
 */
public class UnsupportedMediaException extends FatalException {
    public UnsupportedMediaException(String message) {
        super(message);
    }
}
