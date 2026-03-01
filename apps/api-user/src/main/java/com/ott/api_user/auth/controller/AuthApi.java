package com.ott.api_user.auth.controller;

import com.ott.common.web.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Auth API", description = "인증/인가 API")
public interface AuthApi {

        @Operation(summary = "Access Token 재발급", description = "access token + refresh token 재발급.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "재발급 성공"),
                        @ApiResponse(responseCode = "401", description = "refreshToken이 없거나 만료/유효하지 않음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response);

        @Operation(summary = "로그아웃", description = "DB refreshToken을 삭제, accessToken/refreshToken 쿠키 제거")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response);
}