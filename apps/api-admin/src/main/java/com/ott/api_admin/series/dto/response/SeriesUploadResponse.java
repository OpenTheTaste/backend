package com.ott.api_admin.series.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시리즈 업로드 응답")
public record SeriesUploadResponse(
        @Schema(type = "Long", description = "생성된 시리즈 ID", example = "10")
        Long seriesId,

        @Schema(type = "String", description = "포스터 S3 object key", example = "series/10/poster/poster.jpg")
        String posterObjectKey,

        @Schema(type = "String", description = "썸네일 S3 object key", example = "series/10/thumbnail/thumb.jpg")
        String thumbnailObjectKey,

        @Schema(type = "String", description = "포스터 업로드용 사전 서명 URL")
        String posterUploadUrl,

        @Schema(type = "String", description = "썸네일 업로드용 사전 서명 URL")
        String thumbnailUploadUrl
) {
}
