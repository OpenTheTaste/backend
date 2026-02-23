package com.ott.api_admin.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 로그인 요청")
public class AdminLoginRequest {

    @Email
    @NotBlank
    @Schema(description = "관리자 이메일", example = "admin@ott.com")
    private String email;

    @NotBlank
    @Schema(description = "비밀번호", example = "password123")
    private String password;
}