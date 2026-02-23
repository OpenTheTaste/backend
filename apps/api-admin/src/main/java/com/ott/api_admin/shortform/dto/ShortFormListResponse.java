package com.ott.api_admin.shortform.dto;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "숏폼 목록 조회 응답")
public record ShortFormListResponse(

        @Schema(type = "Long", description = "미디어 ID", example = "1")
        Long mediaId,

        @Schema(type = "String", description = "포스터(세로, 5:7) URL", example = "https://cdn.example.com/thumbnail.jpg")
        String posterUrl,

        @Schema(type = "String", description = "숏폼 제목", example = "비밀의 숲 명장면")
        String title,

        @Schema(type = "String", description = "공개 여부", example = "PUBLIC")
        PublicStatus publicStatus,

        @Schema(type = "LocalDate", description = "업로드일", example = "2026-01-15")
        LocalDate uploadedDate
) {
}
