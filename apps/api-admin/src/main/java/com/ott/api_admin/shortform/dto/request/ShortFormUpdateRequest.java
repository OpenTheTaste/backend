package com.ott.api_admin.shortform.dto.request;

import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "숏폼 수정 요청")
public record ShortFormUpdateRequest(
        @Schema(type = "Long", description = "원본 콘텐츠 ID", example = "1")
        @NotNull
        Long originId,

        @Schema(type = "String", description = "원본 콘텐츠 타입", example = "SERIES")
        @NotNull
        MediaType mediaType,

        @Schema(type = "String", description = "숏폼 제목", example = "하이라이트 수정")
        @NotBlank
        String title,

        @Schema(type = "String", description = "숏폼 설명", example = "명장면 하이라이트 수정")
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

        @Schema(type = "String", description = "포스터 원본 파일명(교체 시에만 입력)", example = "poster-new.jpg")
        String posterFileName,

        @Schema(type = "String", description = "썸네일 원본 파일명(교체 시에만 입력)", example = "thumb-new.jpg")
        String thumbnailFileName,

        @Schema(type = "String", description = "원본 영상 파일명(교체 시에만 입력)", example = "origin-new.mp4")
        String originFileName
) {
}
