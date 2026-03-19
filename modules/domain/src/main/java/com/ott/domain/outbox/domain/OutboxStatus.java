package com.ott.domain.outbox.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboxStatus {
    PENDING("PENDING", "PENDING"),
    PUBLISHED("PUBLISHED", "PUBLISHED"),
    FAILED("FAILED", "FAILED");

    private final String key;
    private final String value;
}
