package com.ott.transcoder.inspection;

import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.inspection.probe.execution.FfprobeExecutor;
import com.ott.transcoder.inspection.validation.FileValidator;
import com.ott.transcoder.inspection.validation.StreamValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * 입력 파일 검사
 * FileValidator → Probe → StreamValidator 순서로 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Inspector {

    private final FileValidator fileValidator;
    private final FfprobeExecutor ffprobeExecutor;
    private final StreamValidator streamValidator;

    public ProbeResult inspect(Path inputFile) {
        fileValidator.validate(inputFile);
        ProbeResult probeResult = ffprobeExecutor.probe(inputFile);
        streamValidator.validate(probeResult);

        return probeResult;
    }
}
