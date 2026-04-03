package com.ott.domain.ingest_command.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {

    TRANSCODE,
    THUMBNAIL

    ;
}
