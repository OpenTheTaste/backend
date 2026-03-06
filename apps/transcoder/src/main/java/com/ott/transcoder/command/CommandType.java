package com.ott.transcoder.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {

    TRANSCODE,
    THUMBNAIL

    ;
}
