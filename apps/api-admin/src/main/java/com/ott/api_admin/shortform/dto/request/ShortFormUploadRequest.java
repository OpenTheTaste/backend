package com.ott.api_admin.shortform.dto.request;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(type = "Object", description = "숏폼 업로드 요청")
public record ShortFormUploadRequest(
        @Schema(type = "Long", description = "연결할 시리즈 ID(선택)", example = "1")
        Long seriesId,

        @Schema(type = "Long", description = "연결할 콘텐츠 ID(선택)", example = "2")
        Long contentsId,

        @Schema(type = "String", description = "숏폼 제목", example = "하이라이트")
        @NotBlank
        String title,

        @Schema(type = "String", description = "숏폼 설명", example = "명장면 하이라이트")
        @NotBlank
        String description,

        @Schema(type = "String", description = "공개 상태", example = "PUBLIC")
        @NotNull
        PublicStatus publicStatus,

        @Schema(type = "Integer", description = "영상 길이(초)", example = "60")
        @PositiveOrZero
        Integer duration,

        @Schema(type = "Integer", description = "영상 크기(KB)", example = "10240")
        @PositiveOrZero
        Integer videoSize,

        @Schema(type = "String", description = "포스터 원본 파일명", example = "poster.jpg")
        @NotBlank
        String posterFileName,

        @Schema(type = "String", description = "썸네일 원본 파일명", example = "thumb.jpg")
        @NotBlank
        String thumbnailFileName,

        @Schema(type = "String", description = "원본 영상 파일명", example = "origin.mp4")
        @NotBlank
        String originFileName
) {
}
