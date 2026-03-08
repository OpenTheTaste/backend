package com.ott.transcoder.command;

public interface Command {

    CommandType getType();
    String getCommandKey();
}
