package com.ott.api_admin.series.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "시리즈 제목 목록 조회 응답")
public record SeriesTitleListResponse(

        @Schema(type = "Long", description = "시리즈 ID", example = "1")
        Long seriesId,

        @Schema(type = "String", description = "시리즈 제목", example = "비밀의 숲")
        String title,

        @Schema(type = "String", description = "카테고리명", example = "드라마")
        String categoryName,

        @Schema(type = "List<String>", description = "태그 이름 목록", example = "[\"스릴러\", \"추리\"]")
        List<String> tagNameList
) {
}
