package com.ott.transcoder.ffmpeg;

import com.ott.domain.video_profile.domain.Resolution;

import java.io.IOException;
import java.nio.file.Path;

/**
 * FFmpeg 실행 추상화 인터페이스
 *
 * FFmpeg를 호출하는 방식(ProcessBuilder, Jaffree 등)에 독립적으로
 * 단일 해상도에 대한 HLS 트랜스코딩을 수행한다.
 *
 * 구현체 전환: transcoder.ffmpeg.engine 프로퍼티로 선택
 *   - processbuilder: ProcessBuilderFfmpegExecutor (CLI 직접 호출)
 *   - jaffree: (향후) JaffreeFfmpegExecutor (라이브러리 호출)
 */
public interface FfmpegExecutor {

    /**
     * 단일 해상도에 대해 HLS 트랜스코딩을 수행한다.
     *
     * @param inputFile  원본 영상 파일 경로
     * @param outputDir  출력 디렉토리 (하위에 360p/, 720p/, 1080p/ 폴더가 생성됨)
     * @param resolution 대상 해상도
     * @return 생성된 미디어 플레이리스트(media.m3u8) 경로
     */
    Path execute(Path inputFile, Path outputDir, Resolution resolution) throws IOException, InterruptedException;
}
