package com.ott.transcoder.inspection.probe.execution;

import com.ott.transcoder.inspection.probe.ProbeResult;

import java.nio.file.Path;

/**
 * ffprobe 실행 추상화 인터페이스
 *
 * 입력 파일의 미디어 메타데이터 추출
 */
public interface FfprobeExecutor {

    /**
     * 입력 파일에 대해 ffprobe를 실행하여 메타데이터 추출
     *
     * @param inputFile 분석 대상 파일 경로
     * @return 추출된 메타데이터
     */
    ProbeResult probe(Path inputFile);
}
