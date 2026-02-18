package com.ott.api_admin.series.dto.response;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "시리즈 목록 조회 응답")
public record SeriesListResponse(

        @Schema(type = "Long", example = "1", description = "시리즈 ID")
        Long seriesId,

        @Schema(type = "String", description = "썸네일 URL", example = "https://cdn.example.com/thumbnail.jpg")
        String thumbnailUrl,

        @Schema(type = "String", description = "시리즈 제목", example = "비밀의 숲")
        String title,

        @Schema(type = "String", description = "카테고리명", example = "드라마")
        String categoryName,

        @Schema(type = "List<String>", description = "태그 이름 목록", example = "[\"스릴러\", \"추리\"]")
        List<String> tagNameList,

        @Schema(type = "String", description = "공개 여부", example = "PUBLIC")
        PublicStatus publicStatus
) {
}
