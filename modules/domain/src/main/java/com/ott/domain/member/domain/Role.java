package com.ott.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    MEMBER("ROLE_MEMBER", "Member"),
    EDITOR("ROLE_EDITOR", "Editor"),
    ADMIN("ROLE_ADMIN", "Admin"),
    SUSPENDED("ROLE_SUSPEND", "Suspended");

    String key;
    String value;
}