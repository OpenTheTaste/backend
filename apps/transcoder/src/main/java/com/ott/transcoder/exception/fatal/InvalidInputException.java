package com.ott.transcoder.exception.fatal;

import com.ott.transcoder.exception.TranscodeErrorCode;

/** 입력 파일이 유효하지 않을 때 (파일 없음, 크기 초과, 포맷 불일치 등) */
public class InvalidInputException extends FatalException {
    public InvalidInputException(TranscodeErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public InvalidInputException(TranscodeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
