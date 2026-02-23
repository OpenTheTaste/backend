package com.ott.api_admin.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리 목록 조회 응답")
public record CategoryListResponse(

        @Schema(type = "Long", description = "카테고리 ID", example = "1")
        Long categoryId,

        @Schema(type = "String", description = "카테고리명", example = "드라마")
        String categoryName
) {
}
