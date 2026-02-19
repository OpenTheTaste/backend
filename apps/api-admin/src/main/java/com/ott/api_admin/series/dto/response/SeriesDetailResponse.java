package com.ott.api_admin.series.dto.response;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "시리즈 상세 조회 응답")
public record SeriesDetailResponse(

        @Schema(type = "Long", description = "시리즈 ID", example = "1")
        Long seriesId,

        @Schema(type = "String", description = "시리즈 제목", example = "비밀의 숲")
        String title,

        @Schema(type = "String", description = "시리즈 설명", example = "검사와 형사의 추리극")
        String description,

        @Schema(type = "String", description = "카테고리명", example = "드라마")
        String categoryName,

        @Schema(type = "List<String>", description = "태그 이름 목록", example = "[\"스릴러\", \"추리\"]")
        List<String> tagNameList,

        @Schema(type = "String", description = "공개 여부", example = "PUBLIC")
        PublicStatus publicStatus,

        @Schema(type = "String", description = "업로더 닉네임", example = "관리자")
        String uploaderNickname,

        @Schema(type = "Long", description = "북마크 수", example = "150")
        Long bookmarkCount,

        @Schema(type = "String", description = "출연진", example = "조승우, 배두나")
        String actors,

        @Schema(type = "String", description = "포스터 URL", example = "https://cdn.ott.com/poster/drama01.jpg")
        String posterUrl,

        @Schema(type = "String", description = "썸네일 URL", example = "https://cdn.ott.com/thumb/drama01.jpg")
        String thumbnailUrl
) {
}
