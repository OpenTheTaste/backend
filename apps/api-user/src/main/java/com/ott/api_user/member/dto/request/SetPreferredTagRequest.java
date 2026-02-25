package com.ott.api_user.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "선호 태그 설정 요청 DTO (온보딩)")
public class SetPreferredTagRequest {

    @NotEmpty(message = "태그를 선택해주세요")
    @NotNull
    @Schema(type ="List", example = "[1, 3, 13]", description = "선택한 태그 ID 목록")
    private List<Long> tagsId;
}
