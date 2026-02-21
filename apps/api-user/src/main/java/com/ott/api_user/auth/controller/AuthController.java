package com.ott.api_user.auth.controller;


import com.ott.api_user.auth.dto.TokenResponse;
import com.ott.api_user.auth.service.AuthService;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Value("${jwt.access-token-expiry}")
    private int accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private int refreshTokenExpiry;


    // Access Token 재발급
    @PostMapping("reissue")
    public ResponseEntity<Void> reissue(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = extractCookie(request, "refreshToken");
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // access + refresh 재발급 -> 보안성 측면
        TokenResponse tokenResponse = authService.reissue(refreshToken);

        // 쿠키에 저장
        addCookie(response, "accessToken", tokenResponse.getAccessToken(), accessTokenExpiry);
        addCookie(response, "refreshToken", tokenResponse.getRefreshToken(), refreshTokenExpiry);

        return ResponseEntity.noContent().build();
    }

    /**
     * 로그아웃
     * DB는 refreshToken 삭제
     * 쿠키는 Controller에서 직접 삭제
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletResponse response) {

        Long memberId = (Long) authentication.getPrincipal();
        authService.logout(memberId);

        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");

        return ResponseEntity.noContent().build();
    }



    // 임시 테스트 코드 -> 추후 프론트 페이지로 변경 예정
    @GetMapping("logincheck")
    public ResponseEntity<Map<String, Object>> logincheck(
            @RequestParam(value = "isNewMember") boolean isNewMember,
            HttpServletRequest request
    ) {
        String accessToken = extractCookie(request, "accessToken");
        String refreshToken = extractCookie(request, "refreshToken");


        return ResponseEntity.ok(Map.of(
                "isNewMember", isNewMember,
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }


    // 인가 테스트용 코드 -> 이렇게 @AuthenticationPrincipal로 쓰시면 됩니다.
    // 추후 memberId -> UserDetails로 리팩토링 예정
    @GetMapping("/me")
    public Long me(@AuthenticationPrincipal Long memberId) {
        return memberId;
    }



    // 쿠키에 대한 접근은 HTTP고 서비스로 내려가면 안되기 때문에 Controller에서 구현
    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie: request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);    // 배포 시 true 변경
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);    // 배포 시 true 변경
        cookie.setPath("/");
        cookie.setMaxAge(0);        // 즉시 삭제
        response.addCookie(cookie);
    }

}
