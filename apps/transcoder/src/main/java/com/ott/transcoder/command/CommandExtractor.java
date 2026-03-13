package com.ott.transcoder.command;

import com.ott.transcoder.ffmpeg.Resolution;
import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.queue.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandExtractor {

    public List<Command> extractCommand(TranscodeMessage message, ProbeResult probeResult) {
        List<Command> commandList = new ArrayList<>();

        for (Resolution resolution : Resolution.values()) {
            if (probeResult.isUpscaleFor(resolution.getHeight())) {
                continue;
            }
            commandList.add(new TranscodeCommand(resolution));
        }

        if (commandList.isEmpty()) {
            log.warn("모든 프로필이 업스케일 대상이라 360p 단일 프로필로 fallback 합니다. mediaId: {}", message.mediaId());
            commandList.add(new TranscodeCommand(Resolution.P360));
        }

        // TODO: Thumbnail, Sprite ...

        return commandList;
    }
}
