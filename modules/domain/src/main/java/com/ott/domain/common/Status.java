package com.ott.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    ACTIVE("ACTIVE", "ACTIVE"),
    DELETE("DELETE", "DELETE");

    String key;
    String value;
}