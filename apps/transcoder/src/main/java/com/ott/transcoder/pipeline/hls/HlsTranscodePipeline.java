package com.ott.transcoder.pipeline.hls;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.ffmpeg.TranscodeProfile;
import com.ott.transcoder.ffmpeg.execution.FfmpegExecutor;
import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.pipeline.CommandPipeline;
import com.ott.transcoder.storage.VideoStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HlsTranscodePipeline implements CommandPipeline {

    private final TranscodePlanner transcodePlanner;
    private final FfmpegExecutor ffmpegExecutor;
    private final MasterPlaylistGenerator masterPlaylistGenerator;
    private final VideoStorage videoStorage;

    @Override
    public void execute(Long mediaId, Path inputFile, Path workDir, ProbeResult probeResult) throws Exception {
        log.info("HLS 트랜스코딩 시작 - mediaId: {}", mediaId);

        // plan
        // TODO: Filter Chain 구성 필요
        List<TranscodeProfile> profileList = transcodePlanner.plan(probeResult);

        // main
        for (TranscodeProfile profile : profileList) {
            ffmpegExecutor.execute(inputFile, workDir, profile);
        }

        // post
        List<Resolution> resolutionList = profileList.stream()
                .map(TranscodeProfile::resolution)
                .toList();
        masterPlaylistGenerator.generate(workDir, resolutionList);

        String uploadedPath = videoStorage.upload(workDir, "media/" + mediaId + "/hls");
        log.info("HLS 트랜스코딩 완료 - mediaId: {}, uploadedPath: {}", mediaId, uploadedPath);
    }
}
