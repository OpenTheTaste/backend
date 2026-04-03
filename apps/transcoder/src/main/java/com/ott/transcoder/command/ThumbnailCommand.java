package com.ott.transcoder.command;

import com.ott.domain.ingest_command.domain.CommandType;
import lombok.Getter;

@Getter
public class ThumbnailCommand implements Command {

    private static final String COMMAND_KEY = "THUMBNAIL";

    @Override
    public CommandType getType() {
        return CommandType.THUMBNAIL;
    }

    @Override
    public String getCommandKey() {
        return COMMAND_KEY;
    }
}