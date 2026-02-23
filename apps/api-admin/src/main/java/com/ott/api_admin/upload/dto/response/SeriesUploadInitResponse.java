package com.ott.api_admin.upload.dto.response;

/**
 * 시리즈 업로드 초기화 응답 DTO입니다.
 */
public record SeriesUploadInitResponse(
        // 생성된 시리즈 ID
        Long seriesId,
        // 포스터 저장 오브젝트 키
        String posterObjectKey,
        // 썸네일 저장 오브젝트 키
        String thumbnailObjectKey,
        // 포스터 업로드용 Presigned URL
        String posterUploadUrl,
        // 썸네일 업로드용 Presigned URL
        String thumbnailUploadUrl
) {
}
