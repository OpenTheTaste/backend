package com.ott.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PublicStatus {
    PUBLIC("PUBLIC", "PUBLIC"),
    PRIVATE("PRIVATE", "PRIVATE");

    String key;
    String value;
}