package com.ott.api_admin.tag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리별 태그 목록 응답")
public record TagResponse(

        @Schema(type = "Long", example = "1", description = "태그 ID")
        Long tagId,

        @Schema(type = "String", example = "로맨스", description = "태그명")
        String name
) {
}
