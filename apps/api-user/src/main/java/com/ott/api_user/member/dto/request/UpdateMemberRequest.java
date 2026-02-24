package com.ott.api_user.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "내 정보 수정 요청 DTO")
public class UpdateMemberRequest {

    @Schema(type = "String", example = "김마루1", description = "변경할 닉네임 / null인 경우 변경 x")
    private String nickname;

    @Schema(type = "List", example = "[1, 3, 14]", description = "변경할 선호 태그 ID 목록 / null이면 변경 x")
    private List<Long> tagIds;
}
