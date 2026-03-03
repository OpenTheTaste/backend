package com.ott.transcoder.exception.retryable;

import com.ott.transcoder.exception.TranscodeErrorCode;

/** 스토리지(로컬/S3) 입출력 실패 */
public class StorageException extends RetryableException {
    public StorageException(TranscodeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
