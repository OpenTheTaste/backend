package com.ott.api_admin.content.dto.request;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@Schema(type = "Object", description = "콘텐츠 업로드 요청")
public record ContentsUploadRequest(
        @Schema(type = "Long", description = "연결할 시리즈 ID(선택)", example = "1")
        Long seriesId,

        @Schema(type = "String", description = "콘텐츠 제목", example = "응답하라 1988 1화")
        @NotBlank
        String title,

        @Schema(type = "String", description = "콘텐츠 설명", example = "가족과 이웃의 따뜻한 이야기")
        @NotBlank
        String description,

        @Schema(type = "String", description = "출연진", example = "성동일, 이일화")
        @NotBlank
        String actors,

        @Schema(type = "String", description = "공개 상태", example = "PUBLIC")
        @NotNull
        PublicStatus publicStatus,

        @Schema(type = "String", description = "카테고리명", example = "드라마")
        @NotBlank
        String categoryName,

        @Schema(type = "List<String>", description = "태그명 목록", example = "[\"가족\", \"코미디\"]")
        @NotEmpty
        List<@NotBlank String> tagNameList,

        @Schema(type = "Integer", description = "영상 길이(초)", example = "3600")
        @PositiveOrZero
        Integer duration,

        @Schema(type = "Integer", description = "영상 크기(KB)", example = "512000")
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
