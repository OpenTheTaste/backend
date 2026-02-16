package com.ott.domain.click_event.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ClickType {
    SHORT_CLICK("SHORT_CLICK", "SHORT_CLICK"),
    CTA_CLICK("CTA_CLICK", "CTA_CLICK");

    String key;
    String value;
}