package com.ott.api_admin.content.dto.request;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "콘텐츠 업로드 요청")
public record ContentsUploadRequest(
        // 연결할 시리즈 ID (없으면 단일 콘텐츠)
        Long seriesId,
        // 콘텐츠 제목
        @NotBlank String title,
        // 콘텐츠 설명
        @NotBlank String description,
        // 출연진 문자열
        @NotBlank String actors,
        // 공개 상태
        @NotNull PublicStatus publicStatus,
        // 영상 길이(초)
        Integer duration,
        // 영상 크기(KB)
        Integer videoSize,
        // 포스터 원본 파일명
        @NotBlank String posterFileName,
        // 썸네일 원본 파일명
        @NotBlank String thumbnailFileName,
        // 원본 영상 파일명
        @NotBlank String originFileName
) {
}
