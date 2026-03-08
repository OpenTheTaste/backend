package com.ott.domain.video_profile.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Resolution {
    P360("360P", "360P", 360, 640, 800_000L, "96k"),
    P720("720P", "720P", 720, 1280, 2_400_000L, "128k"),
    P1080("1080P", "1080P", 1080, 1920, 4_800_000L, "192k");

    private final String key;
    private final String value;
    private final int height;
    private final int width;
    private final long videoBitrate;
    private final String audioBitrate;
}
