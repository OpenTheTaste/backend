package com.ott.api_admin.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘텐츠 업로드 응답")
public record ContentsUploadResponse(
        @Schema(type = "Long", description = "생성된 콘텐츠 ID", example = "10")
        Long contentsId,

        @Schema(type = "String", description = "포스터 S3 object key", example = "contents/10/poster/poster.jpg")
        String posterObjectKey,

        @Schema(type = "String", description = "썸네일 S3 object key", example = "contents/10/thumbnail/thumb.jpg")
        String thumbnailObjectKey,

        @Schema(type = "String", description = "원본 영상 S3 object key", example = "contents/10/origin/origin.mp4")
        String originObjectKey,

        @Schema(type = "String", description = "트랜스코딩 마스터 플레이리스트 object key", example = "contents/10/transcoded/master.m3u8")
        String masterPlaylistObjectKey,

        @Schema(type = "String", description = "포스터 업로드용 사전 서명 URL")
        String posterUploadUrl,

        @Schema(type = "String", description = "썸네일 업로드용 사전 서명 URL")
        String thumbnailUploadUrl,

        @Schema(type = "String", description = "원본 영상 업로드용 사전 서명 URL")
        String originUploadUrl
) {
}
