package com.ott.transcoder.exception.retryable;

import com.ott.transcoder.exception.TranscodeErrorCode;

/** ffprobe 실행/파싱 실패 */
public class ProbeException extends RetryableException {
    public ProbeException(TranscodeErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ProbeException(TranscodeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
