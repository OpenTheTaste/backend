package com.ott.api_admin.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * reissue 시 Service → Controller 토큰 전달용
 */
@Getter
@AllArgsConstructor
public class AdminTokenResponse {

    @Schema(type = "String", description = "accessToken", example = "122Wjxf@djx1jcmxsizkds2fj-dsm2.dzj2")
    private String accessToken;

    @Schema(type = "String", description = "refreshToken", example = "eym122Wjxf@djx1jcmxsizkds2fj-dsm2.dzj2")
    private String refreshToken;
}