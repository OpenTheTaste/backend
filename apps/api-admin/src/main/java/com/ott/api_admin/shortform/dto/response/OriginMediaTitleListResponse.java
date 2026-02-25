package com.ott.api_admin.shortform.dto.response;

import com.ott.domain.common.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "원본 콘텐츠 제목 목록 조회 응답")
public record OriginMediaTitleListResponse(

        @Schema(type = "Long", description = "원본 콘텐츠 ID", example = "1")
        Long originId,

        @Schema(type = "String", description = "원본 콘텐츠 제목", example = "비밀의 숲")
        String title,

        @Schema(type = "String", description = "원본 콘텐츠 타입", example = "SERIES")
        MediaType mediaType
) {
}
