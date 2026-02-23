package com.ott.infra.s3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class S3PresignService {

    private final S3Presigner s3Presigner;
    private final String region;
    private final String bucket;
    private final String publicBaseUrl;
    private final long expireSeconds;

    public S3PresignService(
            S3Presigner s3Presigner,
            @Value("${aws.region:ap-northeast-2}") String region,
            @Value("${aws.s3.bucket:local-bucket}") String bucket,
            @Value("${aws.s3.public-base-url:}") String publicBaseUrl,
            @Value("${aws.s3.presign-expire-seconds:600}") long expireSeconds
    ) {
        this.s3Presigner = s3Presigner;
        this.region = region;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
        this.expireSeconds = expireSeconds;
    }

    public String createPutPresignedUrl(String objectKey, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expireSeconds))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    // objectKey를 실제 S3 객체 URL 형식으로 변환합니다.
    // - 한글/공백/특수문자 깨짐 방지를 위해 URL 인코딩
    // - 공백은 '+' 대신 '%20'으로 정규화
    // - 경로 구분자는 유지하기 위해 '%2F'를 '/'로 복원
    public String toObjectUrl(String objectKey) {
        String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%2F", "/");

        // public-base-url이 설정되어 있으면 우선 사용하고, 없으면 기존 S3 URL 규칙으로 fallback합니다.
        String baseUrl = (publicBaseUrl == null || publicBaseUrl.isBlank())
                ? "https://" + bucket + ".s3." + region + ".amazonaws.com"
                : publicBaseUrl.replaceAll("/+$", "");
        return baseUrl + "/" + encodedKey;
    }
}
