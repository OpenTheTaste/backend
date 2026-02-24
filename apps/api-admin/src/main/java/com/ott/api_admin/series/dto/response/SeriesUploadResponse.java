package com.ott.api_admin.series.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시리즈 업로드 응답")
public record SeriesUploadResponse(
        // 생성된 시리즈 ID
        Long seriesId,
        // 포스터 S3 object key
        String posterObjectKey,
        // 썸네일 S3 object key
        String thumbnailObjectKey,
        // 포스터 업로드용 Presigned URL
        String posterUploadUrl,
        // 썸네일 업로드용 Presigned URL
        String thumbnailUploadUrl
) {
}