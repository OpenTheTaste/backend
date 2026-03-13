package com.ott.infra.s3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
public class S3PresignService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final String region;
    private final String bucket;
    private final String publicBaseUrl;
    private final long expireSeconds;

    public S3PresignService(
            S3Presigner s3Presigner,
            S3Client s3Client,
            @Value("${aws.region:ap-northeast-2}") String region,
            @Value("${aws.s3.bucket:local-bucket}") String bucket,
            @Value("${aws.s3.public-base-url:}") String publicBaseUrl,
            @Value("${aws.s3.presign-expire-seconds:600}") long expireSeconds
    ) {
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
        this.region = region;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
        this.expireSeconds = expireSeconds;
    }

    public String createPutPresignedUrl(String objectKey, String contentType) {
        try {
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
        } catch (SdkException ex) {
            throw new IllegalStateException("Failed to create upload URL.", ex);
        }
    }

    public String createMultipartUpload(String objectKey, String contentType) {
        try {
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                    CreateMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(contentType)
                            .build()
            );
            return response.uploadId();
        } catch (SdkException ex) {
            throw new IllegalStateException("Failed to initialize multipart upload.", ex);
        }
    }

    // Generates a presigned URL locally via SDK signing logic.
    // No outbound request to S3 is made at this step.
    public String createUploadPartPresignedUrl(String objectKey, String uploadId, int partNumber) {
        try {
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();

            UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expireSeconds))
                    .uploadPartRequest(uploadPartRequest)
                    .build();

            PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
            return presignedRequest.url().toString();
        } catch (SdkException ex) {
            throw new IllegalStateException("Failed to create multipart upload part URL.", ex);
        }
    }

    public void completeMultipartUpload(String objectKey, String uploadId, List< MultipartPartETag> partETags) {
        try {
            List<CompletedPart> completedParts = partETags.stream()
                    .map(part -> CompletedPart.builder()
                            .partNumber(part.partNumber())
                            .eTag(part.eTag())
                            .build())
                    .toList();

            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            s3Client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .uploadId(uploadId)
                            .multipartUpload(completedMultipartUpload)
                            .build()
            );
        } catch (SdkException ex) {
            throw new IllegalStateException("Failed to complete multipart upload.", ex);
        }
    }

    // TODO: Incomplete multipart uploads are currently cleaned up by S3 Lifecycle TTL policy.
//    public void abortMultipartUpload(String objectKey, String uploadId) {
//        try {
//            s3Client.abortMultipartUpload(
//                    AbortMultipartUploadRequest.builder()
//                            .bucket(bucket)
//                            .key(objectKey)
//                            .uploadId(uploadId)
//                            .build()
//            );
//        } catch (SdkException ex) {
//            throw new IllegalStateException("Failed to abort multipart upload.", ex);
//        }
//    }

    public String toObjectUrl(String objectKey) {
        String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%2F", "/");

        // Use public-base-url when configured; otherwise fall back to the default S3 URL format.
        String baseUrl = (publicBaseUrl == null || publicBaseUrl.isBlank())
                ? "https://" + bucket + ".s3." + region + ".amazonaws.com"
                : publicBaseUrl.replaceAll("/+$", "");
        return baseUrl + "/" + encodedKey;
    }

    public record MultipartPartETag(
            int partNumber,
            String eTag
    ) {
    }
}
