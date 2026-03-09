package com.ott.api_admin.shortform.dto.request;

import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

        @Schema(type = "String", description = "포스터 원본 파일명(교체 시에만 입력)", example = "poster-new.jpg")
        String posterFileName
) {
}
