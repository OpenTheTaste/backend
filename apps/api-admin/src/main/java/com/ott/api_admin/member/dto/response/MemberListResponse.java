package com.ott.api_admin.member.dto.response;

import com.ott.domain.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "사용자 목록 조회 응답")
public record MemberListResponse(

        @Schema(type = "Long", description = "사용자 ID", example = "1")
        Long memberId,

        @Schema(type = "String", description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(type = "String", description = "이메일", example = "user@example.com")
        String email,

        @Schema(type = "String", description = "역할", example = "MEMBER")
        Role role,

        @Schema(type = "String", description = "가입일", example = "2026-01-15")
        LocalDate createdDate
) {
}
