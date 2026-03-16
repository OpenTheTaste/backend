package com.ott.domain.ingest_command.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommandStatus {
    PENDING("PENDING", "PENDING"),
    COMPLETED("COMPLETED", "COMPLETED");

    String key;
    String value;
}
