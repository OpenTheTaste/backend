package com.ott.transcoder.queue;

import com.ott.domain.common.MediaType;

/**
 * 트랜스코딩 요청 메시지 DTO.
 *
 * @param mediaId   트랜스코딩 대상 미디어 ID (Contents 또는 ShortForm의 media_id)
 * @param ingestJobId 인제스트 작업 ID
 * @param originUrl 원본 영상 위치 (로컬 경로 또는 S3 key)
 * @param fileSize  원본 파일 크기 (bytes, nullable)
 * @param mediaType 미디어 타입 (CONTENTS 또는 SHORT_FORM)
 */
public record TranscodeMessage(

        Long mediaId,
        Long ingestJobId,
        String originUrl,
        Long fileSize,
        MediaType mediaType
) {
}
