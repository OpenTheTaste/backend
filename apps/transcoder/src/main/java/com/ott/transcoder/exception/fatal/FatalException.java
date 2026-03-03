package com.ott.transcoder.exception.fatal;

import com.ott.transcoder.exception.TranscodeException;

// 치명적인 오류로 인해 작업을 즉시 중단해야 하는 경우 발생하는 예외
public class FatalException extends TranscodeException {
    public FatalException(String message) {
        super(message);
    }

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }
}
