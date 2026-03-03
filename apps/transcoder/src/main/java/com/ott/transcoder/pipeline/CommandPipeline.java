package com.ott.transcoder.pipeline;

import com.ott.transcoder.inspection.probe.ProbeResult;

import java.nio.file.Path;

/**
 * 커맨드별 미디어 처리 파이프라인
 * 구현체는 미디어 처리 자체에만 집중
 */
public interface CommandPipeline {

    void execute(Long mediaId, Path inputFile, Path workDir, ProbeResult probeResult);
}
