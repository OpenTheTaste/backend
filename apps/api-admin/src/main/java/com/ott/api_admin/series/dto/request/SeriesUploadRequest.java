package com.ott.api_admin.series.dto.request;

import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "시리즈 업로드 요청")
public record SeriesUploadRequest(
        // 시리즈 제목
        @NotBlank String title,
        // 시리즈 설명
        @NotBlank String description,
        // 출연진 문자열
        @NotBlank String actors,
        // 공개 상태
        @NotNull PublicStatus publicStatus,
        // 포스터 원본 파일명
        @NotBlank String posterFileName,
        // 썸네일 원본 파일명
        @NotBlank String thumbnailFileName
) {
}