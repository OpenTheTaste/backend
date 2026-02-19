package com.ott.api_admin.member.dto.request;

import com.ott.domain.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 역할 변경 요청")
public record ChangeRoleRequest(

        @NotNull(message = "변경할 역할은 필수입니다.")
        @Schema(description = "변경할 역할 (EDITOR 또는 SUSPENDED)", example = "SUSPENDED")
        Role role
) {
}
