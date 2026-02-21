package com.ott.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MediaType {
    SERIES("SERIES", "SERIES"),
    CONTENTS("CONTENTS", "CONTENTS"),
    SHORT_FORM("SHORT_FORM", "SHORT_FORM");

    String key;
    String value;
}
