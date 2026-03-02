package com.ott.api_admin.auth.controller;

import com.ott.api_admin.auth.dto.request.AdminLoginRequest;
import com.ott.api_admin.auth.dto.response.AdminLoginResponse;
import com.ott.api_admin.auth.dto.response.AdminTokenResponse;
import com.ott.api_admin.auth.service.AdminAuthService;
import com.ott.common.security.util.CookieUtil;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.SuccessResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/back-office")
@RequiredArgsConstructor
public class AdminAuthController implements AdminAuthApi {

    private final AdminAuthService adminAuthService;
    private final CookieUtil cookie;

    @Value("${jwt.access-token-expiry}")
    private int accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private int refreshTokenExpiry;

    @Override
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse response) {

        AdminLoginResponse loginResponse = adminAuthService.login(request);

        // 둘 다 쿠키로
        cookie.addCookie(response, "accessToken", loginResponse.getAccessToken(), accessTokenExpiry);
        cookie.addCookie(response, "refreshToken", loginResponse.getRefreshToken(), refreshTokenExpiry);

        // Body에는 memberId, role만 (토큰은 @JsonIgnore)
        return SuccessResponse.of(loginResponse).asHttp(HttpStatus.OK);
    }

    @Override
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = extractCookie(request, "refreshToken");
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        AdminTokenResponse tokenResponse = adminAuthService.reissue(refreshToken);

        cookie.addCookie(response, "accessToken", tokenResponse.getAccessToken(), accessTokenExpiry);
        cookie.addCookie(response, "refreshToken", tokenResponse.getRefreshToken(), refreshTokenExpiry);

        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletResponse response) {

        Long memberId = (Long) authentication.getPrincipal();
        adminAuthService.logout(memberId);

        cookie.deleteCookie(response, "accessToken");
        cookie.deleteCookie(response, "refreshToken");

        return ResponseEntity.noContent().build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}