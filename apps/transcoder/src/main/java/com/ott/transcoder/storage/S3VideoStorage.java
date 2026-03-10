package com.ott.transcoder.storage;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * AWS S3 기반 VideoStorage 구현체
 *
 * - download: S3에서 workDir로 파일 다운로드
 * - upload: workDir 내 모든 파일을 S3에 업로드
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3VideoStorage implements VideoStorage {

    private final S3Client s3Client;
    private final String bucket;

    public S3VideoStorage(
            S3Client s3Client,
            @Value("${storage.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public Path download(String sourceKey, Path workDir) {
        String fileName = sourceKey.contains("/")
                ? sourceKey.substring(sourceKey.lastIndexOf('/') + 1)
                : sourceKey;
        Path target = workDir.resolve(fileName);

        try {
            s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(sourceKey)
                            .build(),
                    target
            );
        } catch (SdkException e) {
            throw new StorageException(TranscodeErrorCode.STORAGE_FAILED,
                    "S3 다운로드 실패 - bucket: " + bucket + ", key: " + sourceKey, e);
        }

        log.info("S3 다운로드 완료 - s3://{}/{} → {}", bucket, sourceKey, target);
        return target;
    }

    @Override
    public String upload(Path localDir, String destinationPrefix) {
        try (Stream<Path> fileStream = Files.walk(localDir)) {
            List<Path> fileList = fileStream.filter(Files::isRegularFile).toList();

            for (Path file : fileList) {
                String relativePath = localDir.relativize(file).toString().replace("\\", "/");
                String s3Key = destinationPrefix + "/" + relativePath;

                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(s3Key)
                                .build(),
                        RequestBody.fromFile(file)
                );

                log.debug("S3 업로드 - {}", s3Key);
            }
        } catch (IOException e) {
            throw new StorageException(TranscodeErrorCode.STORAGE_FAILED,
                    "S3 업로드 중 파일 탐색 실패 - " + localDir, e);
        } catch (SdkException e) {
            throw new StorageException(TranscodeErrorCode.STORAGE_FAILED,
                    "S3 업로드 실패 - bucket: " + bucket + ", prefix: " + destinationPrefix, e);
        }

        log.info("S3 업로드 완료 - {} → s3://{}/{}", localDir, bucket, destinationPrefix);
        return destinationPrefix;
    }
}
