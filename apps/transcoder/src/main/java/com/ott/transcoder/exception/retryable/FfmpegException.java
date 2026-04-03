package com.ott.transcoder.exception.retryable;

import com.ott.transcoder.exception.TranscodeErrorCode;

/** FFmpeg 실행 실패/타임아웃 */
public class FfmpegException extends RetryableException {
    public FfmpegException(TranscodeErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public FfmpegException(TranscodeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
