package com.ott.domain.ingest_job.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IngestStatus {
    PENDING("PENDING", "PENDING"),
    PROCESSING("PROCESSING", "PROCESSING"),
    PARTIAL_SUCCESS("PARTIAL_SUCCESS", "PARTIAL_SUCCESS"),
    SUCCESS("SUCCESS", "SUCCESS"),
    FAILED("FAILED", "FAILED");

    String key;
    String value;
}