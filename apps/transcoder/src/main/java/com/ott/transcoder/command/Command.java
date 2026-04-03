package com.ott.transcoder.command;

import com.ott.domain.ingest_command.domain.CommandType;

public interface Command {

    CommandType getType();
    String getCommandKey();
}
