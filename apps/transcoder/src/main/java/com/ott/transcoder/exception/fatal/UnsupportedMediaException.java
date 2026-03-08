package com.ott.transcoder.exception.fatal;

import com.ott.transcoder.exception.TranscodeErrorCode;

/** 지원하지 않는 코덱이나 미디어 스펙일 때 */
public class UnsupportedMediaException extends FatalException {
    public UnsupportedMediaException(TranscodeErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
