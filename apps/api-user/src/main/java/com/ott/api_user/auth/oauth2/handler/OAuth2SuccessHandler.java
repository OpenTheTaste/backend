package com.ott.api_user.auth.oauth2.handler;

import com.ott.api_user.auth.service.KakaoAuthService;
import com.ott.common.security.util.CookieUtil;
import com.ott.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;

/**
 * Oauth2 성공시 해당 핸들러 자동 호출
 * 카카오 로그인 성공시 해당 핸들러에서 처리
 * JWT 생성(Access, Refresh)
 * Refresh Token DB 저장
 * 콜백 URL로 리다이렉트 + 만든 토큰은 쿠키로 전달
 */

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoAuthService kakaoAuthService;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend-url}")
    private String frontedUrl;

    // 30분
    @Value("${jwt.access-token-expiry}")
    private int accessTokenExpiry;

    // 14일
    @Value("${jwt.refresh-token-expiry}")
    private int refreshTokenExpiry;

    // Oauth2 로그인 성공 시 해당 메소드를 스프링 스큐리티가 자동 호출
    // 이 시점에서 authenication에 로그인된 사용자 정보가 저장 user-info
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // memberId, authroies, isNewMember의 결과가 map으로 저장
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        long memberId = ((Number) principal.getAttributes().get("memberId")).longValue();
        boolean isNewMember = (boolean) principal.getAttributes().get("isNewMember");

        // authorties: ["ROLE_MEMBER"]
        List<String> authorties = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(memberId, authorties);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId, authorties);

        kakaoAuthService.saveRefreshToken(memberId, refreshToken);

        // 쿠키로 저장
        cookieUtil.addCookie(response, "accessToken", accessToken, accessTokenExpiry);
        cookieUtil.addCookie(response, "refreshToken", refreshToken, refreshTokenExpiry);


        // 리다이렉트에는 isNewMember에 따라서 경로 변경
        String targetUrl = isNewMember
                ? frontedUrl + "/auth/userinfo"
                : frontedUrl + "/";

        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }

}