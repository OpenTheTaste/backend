package com.ott.api_admin.auth.controller;

import com.ott.api_admin.auth.dto.request.AdminLoginRequest;
import com.ott.api_admin.auth.dto.response.AdminLoginResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin Auth API", description = "관리자 인증/인가 API")
public interface AdminAuthApi {

    @Operation(
            summary = "관리자 로그인",
            description = "이메일/비밀번호로 로그인합니다. "
                    + "Access Token과 Refresh Token은 HttpOnly 쿠키로 세팅됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<SuccessResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse response);

    @Operation(
            summary = "토큰 재발급",
            description = "refreshToken 쿠키를 사용해 Access Token과 Refresh Token을 재발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "재발급 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "refreshToken이 없거나 만료/유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response);

    @Operation(
            summary = "로그아웃",
            description = "DB의 refreshToken을 삭제하고 accessToken/refreshToken 쿠키를 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response);
}