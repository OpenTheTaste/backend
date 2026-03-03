package com.ott.transcoder.storage;

import java.nio.file.Path;

/**
 * 영상 파일 저장소 추상화 인터페이스
 * S3VideoStorage를 추가하여 실제 AWS S3 연동으로 교체할 것
 */
public interface VideoStorage {

    /**
     * 원본 영상을 저장소에서 로컬 작업 디렉토리로 가져온다
     * @param sourceKey 원본 위치 (로컬 경로 또는 S3 key)
     * @param workDir   다운로드 대상 로컬 디렉토리
     * @return 다운로드된 로컬 파일 경로
     */
    Path download(String sourceKey, Path workDir);

    /**
     * 트랜스코딩 결과물을 저장소에 업로드
     * @param localDir          업로드할 로컬 디렉토리 (HLS 파일들이 들어있음)
     * @param destinationPrefix 저장소 내 목적지 경로 (예: "media/1/hls")
     * @return 업로드된 경로 (DB에 저장할 URL 또는 경로)
     */
    String upload(Path localDir, String destinationPrefix);
}
