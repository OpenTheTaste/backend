package com.ott.transcoder.command;

import com.ott.domain.ingest_command.domain.CommandType;
import com.ott.transcoder.ffmpeg.Resolution;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TranscodeCommand implements Command {

    private final Resolution resolution;

    @Override
    public CommandType getType() {
        return CommandType.TRANSCODE;
    }

    @Override
    public String getCommandKey() {
        return resolution.getKey();
    }
}
