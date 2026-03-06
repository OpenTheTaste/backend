package com.ott.transcoder.command;

import com.ott.domain.video_profile.domain.Resolution;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TranscodeCommand implements Command {

    Resolution resolution;

    @Override
    public CommandType getType() {
        return CommandType.TRANSCODE;
    }

    @Override
    public String getCommandKey() {
        return resolution.getKey();
    }
}
