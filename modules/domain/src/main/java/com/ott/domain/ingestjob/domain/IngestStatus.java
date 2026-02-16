package com.ott.domain.ingestjob.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IngestStatus {
    ORIGIN_UPLOADED("ORIGIN_UPLOADED", "ORIGIN_UPLOADED"),
    TRANSCODING("TRANSCODING", "TRANSCODING"),
    UPLOADING("UPLOADING", "UPLOADING"),
    COMPLETED("COMPLETED", "COMPLETED");

    String key;
    String value;
}