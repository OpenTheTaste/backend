package com.ott.api_admin.series.dto.request;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "시리즈 업로드 요청")
public record SeriesUploadRequest(
        @Schema(type = "String", description = "시리즈 제목", example = "응답하라 1988")
        @NotBlank
        String title,

        @Schema(type = "String", description = "시리즈 설명", example = "가족과 이웃의 따뜻한 이야기")
        @NotBlank
        String description,

        @Schema(type = "String", description = "출연진", example = "성동일, 이일화")
        @NotBlank
        String actors,

        @Schema(type = "String", description = "공개 상태", example = "PUBLIC")
        @NotNull
        PublicStatus publicStatus,

        @Schema(type = "Long", description = "카테고리 ID", example = "1")
        @NotNull
        @Positive
        Long categoryId,

        @Schema(type = "List<Long>", description = "태그 ID 목록", example = "[1, 2]")
        @NotEmpty
        List<@NotNull @Positive Long> tagIdList,

        @Schema(type = "String", description = "포스터 원본 파일명", example = "poster.jpg")
        @NotBlank
        String posterFileName,

        @Schema(type = "String", description = "썸네일 원본 파일명", example = "thumb.jpg")
        @NotBlank
        String thumbnailFileName
) {
}
