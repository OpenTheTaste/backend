package com.ott.api_admin.tag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그별 시청 통계 응답")
public record TagViewResponse(

        @Schema(type = "String", description = "태그명", example = "스릴러")
        String tagName,

        @Schema(type = "Long", description = "당월 시청 수", example = "120")
        Long viewCount
) {
}
