package com.ott.api_admin.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

import com.ott.domain.common.PublicStatus;

@Schema(description = "콘텐츠 목록 조회 응답")
public record ContentsListResponse(

                @Schema(type = "Long", description = "미디어 ID", example = "1") Long mediaId,

                @Schema(type = "String", description = "포스터(세로, 5:7) URL", example = "https://cdn.example.com/thumbnail.jpg") String posterUrl,

                @Schema(type = "String", description = "콘텐츠 제목", example = "기생충") String title,

                @Schema(type = "String", description = "공개 여부", example = "PUBLIC") PublicStatus publicStatus,

                @Schema(type = "LocalDate", description = "업로드일", example = "2026-01-15") LocalDate uploadedDate) {
}
