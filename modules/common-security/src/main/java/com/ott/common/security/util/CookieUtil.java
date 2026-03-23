package com.ott.common.security.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

//    // =====================add=====================
//    // cdn도 해당 유틸 함수를 사용하고 있어서 오버로딩으로 분리
//    // refresh, access 토큰의 경우 .domain를 명시하지 않아서 도메인을 분리
//    // cdn의 경우 서브도메인 허용
//
//    // 기존 호출부 그대로 유지 - accessToken/refreshToken용 (domain 없음 = 서브도메인 격리)
//    public void addCookie(HttpServletResponse response, String name, String value, int maxAgeMillis) {
//        buildCookie(response, name, value, maxAgeMillis, null);
//    }
//
//    // CloudFront 쿠키용 - domain 명시 필요할 때
//    public void addCookie(HttpServletResponse response, String name, String value, int maxAgeMillis, String domain) {
//        buildCookie(response, name, value, maxAgeMillis, domain);
//    }
//
//    private void buildCookie(HttpServletResponse response, String name, String value, int maxAgeMillis, String domain) {
//        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
//                .httpOnly(true)
//                .secure(true)
//                .path("/")
//                .maxAge(maxAgeMillis / 1000)
//                .sameSite("None");
//
//        if (domain != null) {
//            builder.domain(domain);
//        }
//
//        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
//    }
//
//    // =====================delete=====================
//
//    public void deleteCookie(HttpServletResponse response, String name) {
//        buildDeleteCookie(response, name, null);
//    }
//
//    public void deleteCookie(HttpServletResponse response, String name, String domain) {
//        buildDeleteCookie(response, name, domain);
//    }
//
//    private void buildDeleteCookie(HttpServletResponse response, String name, String domain) {
//        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
//                .httpOnly(true)
//                .secure(true)
//                .path("/")
//                .maxAge(0)
//                .sameSite("None");
//
//        if (domain != null) {
//            builder.domain(domain);
//        }
//
//        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
//    }



    public void addCookie(HttpServletResponse response, String name, String value, int maxAgeMillis) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .domain("openthetaste.cloud")  // 로컬 테스트 시 주석처리!!!
                .httpOnly(true) // JS 접근 차단 -> 크로스 사이트 스크립트 공격 대비
                .secure(true) // HTTPS 요청만 허용
                .path("/")  // 모든 경로로 전송
                .maxAge(maxAgeMillis / 1000) // 밀리초 → 초 변환
                .sameSite("None")  // 크로스 사이트에 대해서 쿠키 전송 허용
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .domain("openthetaste.cloud") // 로컬 테스트 시 주석처리!!!
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}