package com.ott.transcoder.exception.fatal;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.TranscodeException;

/** 재시도 불가 — 즉시 FAILED 처리 */
public class FatalException extends TranscodeException {
    public FatalException(TranscodeErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public FatalException(TranscodeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
