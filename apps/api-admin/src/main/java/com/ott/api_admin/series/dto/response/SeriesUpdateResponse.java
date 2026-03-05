package com.ott.api_admin.series.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시리즈 수정 응답")
public record SeriesUpdateResponse(
        @Schema(type = "Long", description = "시리즈 ID", example = "10")
        Long seriesId,

        @Schema(type = "String", description = "포스터 Object Key (교체하지 않으면 null)", example = "series/10/poster/poster-new.jpg")
        String posterObjectKey,

        @Schema(type = "String", description = "썸네일 Object Key (교체하지 않으면 null)", example = "series/10/thumbnail/thumb-new.jpg")
        String thumbnailObjectKey,

        @Schema(type = "String", description = "포스터 업로드 URL(교체하지 않으면 null)", example = "https://oplust-content.s3.ap-northeast-2.amazonaws.com/series/10/poster/poster-new.jpg?X-Amz-.../~")
        String posterUploadUrl,

        @Schema(type = "String", description = "썸네일 업로드 URL(교체하지 않으면 null)", example = "https://oplust-content.s3.ap-northeast-2.amazonaws.com/series/10/thumbnail/thumb-new.jpg?X-Amz-.../~")
        String thumbnailUploadUrl
) {
}