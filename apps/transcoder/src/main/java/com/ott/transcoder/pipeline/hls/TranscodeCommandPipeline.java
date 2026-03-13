package com.ott.transcoder.pipeline.hls;

import com.ott.transcoder.command.Command;
import com.ott.domain.ingest_command.domain.CommandType;
import com.ott.transcoder.command.TranscodeCommand;
import com.ott.transcoder.ffmpeg.TranscodeProfile;
import com.ott.transcoder.ffmpeg.execution.FfmpegExecutor;
import com.ott.transcoder.job.JobContext;
import com.ott.transcoder.pipeline.CommandPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TranscodeCommandPipeline implements CommandPipeline<TranscodeCommand> {

    private final TranscodePlanner transcodePlanner;
    private final FfmpegExecutor ffmpegExecutor;

    @Override
    public boolean support(Command command) {
        return CommandType.TRANSCODE.equals(command.getType());
    }

    @Override
    public void execute(TranscodeCommand command, JobContext jobContext) {
        // 1. plan: 커맨드 + probeResult 기반으로 트랜스코딩 프로파일 결정
        TranscodeProfile profile = transcodePlanner.plan(command, jobContext.probeResult());

        log.info("HLS 트랜스코딩 시작 - mediaId: {}, resolution: {}",
                jobContext.mediaId(), profile.resolution().getKey());

        // 2. main: FFmpeg 실행
        ffmpegExecutor.execute(jobContext.inputFile(), jobContext.outputDir(), profile);

        log.info("HLS 트랜스코딩 완료 - mediaId: {}, resolution: {}",
                jobContext.mediaId(), profile.resolution().getKey());
    }
}
