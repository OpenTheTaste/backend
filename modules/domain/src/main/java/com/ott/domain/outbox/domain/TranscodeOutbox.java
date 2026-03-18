package com.ott.domain.outbox.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.common.MediaType;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name = "transcode_outbox")
public class TranscodeOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "ingest_job_id", nullable = false)
    private Long ingestJobId;

    @Column(name = "origin_url", nullable = false, length = 500)
    private String originUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbox_status", nullable = false)
    private OutboxStatus outboxStatus;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Builder
    public TranscodeOutbox(Long mediaId, Long ingestJobId, String originUrl, Long fileSize, MediaType mediaType, int maxRetries) {
        this.mediaId = mediaId;
        this.ingestJobId = ingestJobId;
        this.originUrl = originUrl;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.outboxStatus = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.maxRetries = (maxRetries > 0) ? maxRetries : 5;
    }

    public void markPublished() {
        this.outboxStatus = OutboxStatus.PUBLISHED;
    }

    public void markFailed(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;
        if (this.retryCount >= this.maxRetries) {
            this.outboxStatus = OutboxStatus.FAILED;
        }
    }
}
