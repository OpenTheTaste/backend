package com.ott.api_admin.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * reissue 시 Service → Controller 토큰 전달용
 */
@Getter
@AllArgsConstructor
public class AdminTokenResponse {
    private String accessToken;
    private String refreshToken;
}