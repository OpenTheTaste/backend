package com.ott.api_admin.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 로그인 응답")
public class AdminLoginResponse {

    @JsonIgnore  // 쿠키로 전달 — JSON 응답에서 제외
    private String accessToken;

    @JsonIgnore  // 쿠키로 전달 — JSON 응답에서 제외
    private String refreshToken;

    @Schema(type = "Long", description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(type= "String", description = "회원 역할", example = "ADMIN")
    private String role;

    @Schema(type = "String", description = "이메일", example = "admin@example.com")
    private String email;

    @Schema(type = "String", description = "닉네임", example = "관리자")
    private String nickname;
}