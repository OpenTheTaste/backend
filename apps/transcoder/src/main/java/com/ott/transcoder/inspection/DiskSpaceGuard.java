package com.ott.transcoder.inspection;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 다운로드 전 디스크 여유 공간 검증
 * 원본 크기 × multiplier만큼의 공간이 있는지 확인
 */
@Slf4j
@Component
public class DiskSpaceGuard {

    @Value("${transcoder.validation.disk-space-multiplier:5}")
    private double multiplier;

    public void check(Path workDir, long fileSize) {
        long requiredSpace = (long)(fileSize * multiplier);
        long usableSpace = workDir.toFile().getUsableSpace();

        if (usableSpace < requiredSpace) {
            throw new StorageException(
                    TranscodeErrorCode.DISK_SPACE_INSUFFICIENT,
                    String.format("디스크 공간 부족 - 필요: %dMB, 가용: %dMB",
                            requiredSpace / 1_000_000, usableSpace / 1_000_000));
        }
    }
}
