package com.ott.api_user.auth.oauth2.userinfo;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoUserInfo {

    private final String providerId;
    private final String email;
    private final String nickname;

    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.providerId = String.valueOf(attributes.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        this.nickname = (String) profile.get("nickname");
        this.email = (String) kakaoAccount.get("email");
    }
}
