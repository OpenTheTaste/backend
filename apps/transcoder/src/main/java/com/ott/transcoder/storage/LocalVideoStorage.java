package com.ott.transcoder.storage;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * 로컬 파일시스템 기반 VideoStorage 구현체
 *
 * 개발/테스트 환경에서 S3 없이 동작하기 위한 구현
 * - download: 로컬 경로에서 workDir로 파일 복사
 * - upload: workDir 내 모든 파일을 output-dir 하위로 재귀 복사
 */
@Slf4j
@Component
//@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalVideoStorage implements VideoStorage {

    @Value("${storage.local.output-dir:#{systemProperties['java.io.tmpdir'] + '/ott-storage'}}")
    private String outputDir;

    @Override
    public Path download(String sourceKey, Path workDir) {
        Path source = Path.of(sourceKey);
        Path target = workDir.resolve(source.getFileName());

        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException(TranscodeErrorCode.STORAGE_FAILED, "원본 파일 다운로드 실패 - source: " + sourceKey, e);
        }

        log.info("원본 다운로드 완료 - {} → {}", sourceKey, target);
        return target;
    }

    @Override
    public String upload(Path localDir, String destinationPrefix) {
        Path destination = Path.of(outputDir, destinationPrefix);

        try (Stream<Path> fileStream = Files.walk(localDir)) {
            Files.createDirectories(destination);

            List<Path> fileList = fileStream.filter(Files::isRegularFile).toList();

            for (Path file : fileList) {
                Path relativePath = localDir.relativize(file);
                Path targetFile = destination.resolve(relativePath);
                Files.createDirectories(targetFile.getParent());
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException(TranscodeErrorCode.STORAGE_FAILED,
                    "업로드 실패 - " + destination, e);
        }

        log.info("업로드 완료 - {} → {}", localDir, destination);
        return destination.toString();
    }
}
