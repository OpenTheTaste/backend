package com.ott.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TargetType {
    SHORT_FORM("SHORT_FORM", "SHORT_FORM"),
    CONTENTS("CONTENTS", "CONTENTS"),
    SERIES("SERIES", "SERIES");

    String key;
    String value;
}