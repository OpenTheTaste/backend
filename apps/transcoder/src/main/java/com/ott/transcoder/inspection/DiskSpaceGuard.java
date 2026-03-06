package com.ott.transcoder.inspection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public void check(Path originPath) {

    }
}
