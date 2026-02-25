package com.ott.transcoder.pipeline;

/**
 * 미디어 처리 파이프라인 추상화 인터페이스
 *
 * 하나의 미디어에 대해 pre → main → post 흐름을 실행하는 커맨드 단위
 * 커맨드 종류에 따라 구현체가 달라진다
 *
 * 현재 구현체: HlsTranscodePipeline (HLS 트랜스코딩)
 * 향후 구현체: ThumbnailPipeline, SpritePipeline 등
 */
public interface CommandPipeline {

    /**
     * 파이프라인을 실행
     *
     * @param mediaId   대상 미디어 ID
     * @param originUrl 원본 영상 위치 (로컬 경로 또는 S3 key)
     */
    void execute(Long mediaId, String originUrl);
}
