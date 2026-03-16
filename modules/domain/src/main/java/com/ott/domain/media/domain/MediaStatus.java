package com.ott.domain.media.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MediaStatus {
    INIT("INIT", "INIT"),
    COMPLETED("COMPLETED", "COMPLETED"),
    FAILED("FAILED", "FAILED");

    String key;
    String value;
}
