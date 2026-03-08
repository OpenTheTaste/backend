package com.ott.transcoder.queue;

/**
 * 트랜스코딩 요청 메시지 DTO.
 *
 * @param mediaId   트랜스코딩 대상 미디어 ID (Contents 또는 ShortForm의 media_id)
 * @param originUrl 원본 영상 위치 (로컬 경로 또는 S3 key)
 */
public record TranscodeMessage(

        Long mediaId,
        Long ingestJobId,
        String originUrl
) {
}
