package com.ott.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Provider {
    LOCAL("LOCAL", "LOCAL"),
    KAKAO("KAKAO", "KAKAO");

    String key;
    String value;
}