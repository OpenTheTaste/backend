package com.ott.transcoder.exception.fatal;

/**
 * 입력 데이터(파일 경로, 파라미터 등)가 유효하지 않을 때 발생하는 예외
 */
public class InvalidInputException extends FatalException {
    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
