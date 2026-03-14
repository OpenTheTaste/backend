package com.ott.transcoder.pipeline.hls;

import com.ott.transcoder.command.Command;
import com.ott.domain.ingest_command.domain.CommandType;
import com.ott.transcoder.command.TranscodeCommand;
import com.ott.transcoder.ffmpeg.TranscodeProfile;
import com.ott.transcoder.ffmpeg.execution.FfmpegExecutor;
import com.ott.transcoder.job.JobContext;
import com.ott.transcoder.pipeline.CommandPipeline;
import com.ott.transcoder.storage.VideoStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
@Component
public class TranscodeCommandPipeline implements CommandPipeline<TranscodeCommand> {

    private final TranscodePlanner transcodePlanner;
    private final FfmpegExecutor ffmpegExecutor;
    private final VideoStorage videoStorage;

    @Override
    public boolean support(Command command) {
        return CommandType.TRANSCODE.equals(command.getType());
    }

    @Override
    public String execute(TranscodeCommand command, JobContext jobContext) {
        // 1. plan: 커맨드 + probeResult 기반으로 트랜스코딩 프로파일 결정
        TranscodeProfile profile = transcodePlanner.plan(command, jobContext.probeResult());

        log.info("HLS 트랜스코딩 시작 - mediaId: {}, resolution: {}",
                jobContext.mediaId(), profile.resolution().getKey());

        // 2. main: FFmpeg 실행
        ffmpegExecutor.execute(jobContext.inputFile(), jobContext.outputDir(), profile);

        log.info("HLS 트랜스코딩 완료 - mediaId: {}, resolution: {}",
                jobContext.mediaId(), profile.resolution().getKey());

        // 3. post: 해상도 디렉토리 S3 업로드
        String commandKey = command.getCommandKey();
        Path resolutionDir = jobContext.outputDir().resolve(commandKey);
        String destinationPrefix = jobContext.uploadPrefix() + "/" + commandKey;
        videoStorage.upload(resolutionDir, destinationPrefix);

        log.info("해상도 업로드 완료 - mediaId: {}, resolution: {}, destination: {}",
                jobContext.mediaId(), commandKey, destinationPrefix);

        return destinationPrefix + "/media.m3u8";
    }
}
