package com.ott.transcoder.command;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.queue.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        // TODO: Thumbnail, Sprite ...

        return commandList;
    }
}
