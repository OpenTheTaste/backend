package com.ott.transcoder.exception.retryable;

/**
 * 스토리지(로컬 파일 시스템, S3 등) 입출력 중 발생하는 예외
 */
public class StorageException extends RetryableException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
