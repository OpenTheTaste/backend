package com.ott.api_admin.series.dto.request;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "시리즈 수정 요청")
public record SeriesUpdateRequest(
        @Schema(description = "시리즈 제목", example = "수정된 시리즈 제목")
        @NotBlank
        String title,

        @Schema(type = "String", description = "시리즈 설명", example = "수정된 시리즈 설명")
        @NotBlank
        String description,

        @Schema(type = "String", description = "출연진", example = "배우A, 배우B")
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

        @Schema(type = "String", description = "새 포스터 파일명 (교체 시에만 입력)", example = "poster-new.jpg")
        String posterFileName,

        @Schema(type = "String", description = "새 썸네일 파일명 (교체 시에만 입력)", example = "thumb-new.jpg")
        String thumbnailFileName
) {
}
