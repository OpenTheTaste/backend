package com.ott.api_admin.upload.dto.response;

/**
 * 숏폼 업로드 초기화 응답 DTO입니다.
 */
public record ShortFormUploadInitResponse(
        // 생성된 숏폼 ID
        Long shortFormId,
        // 포스터 저장 오브젝트 키
        String posterObjectKey,
        // 썸네일 저장 오브젝트 키
        String thumbnailObjectKey,
        // 원본 영상 저장 오브젝트 키
        String originObjectKey,
        // 트랜스코딩 결과 마스터 플레이리스트 오브젝트 키
        String masterPlaylistObjectKey,
        // 포스터 업로드용 Presigned URL
        String posterUploadUrl,
        // 썸네일 업로드용 Presigned URL
        String thumbnailUploadUrl,
        // 원본 영상 업로드용 Presigned URL
        String originUploadUrl
) {
}
