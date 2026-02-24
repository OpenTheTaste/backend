package com.ott.api_admin.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘텐츠 업로드 응답")
public record ContentsUploadResponse(
        // 생성된 콘텐츠 ID
        Long contentsId,
        // 포스터 S3 object key
        String posterObjectKey,
        // 썸네일 S3 object key
        String thumbnailObjectKey,
        // 원본 영상 S3 object key
        String originObjectKey,
        // 트랜스코딩 결과 마스터 플레이리스트 object key
        String masterPlaylistObjectKey,
        // 포스터 업로드용 Presigned URL
        String posterUploadUrl,
        // 썸네일 업로드용 Presigned URL
        String thumbnailUploadUrl,
        // 원본 영상 업로드용 Presigned URL
        String originUploadUrl
) {
}