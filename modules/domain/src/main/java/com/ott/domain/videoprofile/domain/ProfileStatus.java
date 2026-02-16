package com.ott.domain.videoprofile.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProfileStatus {
    COMPLETED("COMPLETED", "COMPLETED");

    String key;
    String value;
}