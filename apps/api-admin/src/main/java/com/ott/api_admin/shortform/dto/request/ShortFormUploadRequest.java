package com.ott.api_admin.shortform.dto.request;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "숏폼 업로드 요청")
public record ShortFormUploadRequest(
        // 연결할 시리즈 ID (선택)
        Long seriesId,
        // 연결할 콘텐츠 ID (선택)
        Long contentsId,
        // 숏폼 제목
        @NotBlank String title,
        // 숏폼 설명
        @NotBlank String description,
        // 공개 상태
        @NotNull PublicStatus publicStatus,
        // 영상 길이(초)
        Integer duration,
        // 영상 크기(바이트 또는 내부 단위)
        Integer videoSize,
        // 포스터 원본 파일명
        @NotBlank String posterFileName,
        // 썸네일 원본 파일명
        @NotBlank String thumbnailFileName,
        // 원본 영상 파일명
        @NotBlank String originFileName
) {
}