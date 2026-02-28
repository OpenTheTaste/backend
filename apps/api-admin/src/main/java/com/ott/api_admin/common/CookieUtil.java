package com.ott.api_admin.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .domain("openthetaste.cloud")  // 로컬 테스트 시 주석처리!!!
                .httpOnly(true) // JS 접근 차단 -> 크로스 사이트 스크립트 공격 대비
                .secure(true) // HTTPS 요청만 허용
                .path("/")  // 모든 경로로 전송
                .maxAge(maxAge)
                .sameSite("None")  // 크로스 사이트에 대해서 쿠키 전송 허용
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .domain("openthetaste.cloud")  // 로컬 테스트 시 주석처리!!!
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}