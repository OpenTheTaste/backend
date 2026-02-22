package com.ott.api_admin.series.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시리즈 제목 목록 조회 응답")
public record SeriesTitleListResponse(

        @Schema(type = "Long", description = "시리즈 ID", example = "1")
        Long seriesId,

        @Schema(type = "String", description = "시리즈 제목", example = "비밀의 숲")
        String title
) {
}
