package com.ott.transcoder.job;

import com.ott.transcoder.inspection.probe.ProbeResult;

import java.nio.file.Path;

public record JobContext(
        Long mediaId,
        Long ingestJobId,
        Path workDir,
        Path inputFile,
        ProbeResult probeResult
) {
}
