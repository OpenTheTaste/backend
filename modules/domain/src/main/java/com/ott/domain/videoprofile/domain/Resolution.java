package com.ott.domain.videoprofile.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Resolution {
    P360("360P", "360P"),
    P720("720P", "720P"),
    P1080("1080P", "1080P");

    private final String key;
    private final String value;
}
