package com.ott.transcoder.pipeline;

import com.ott.transcoder.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CommandPipelineFactory {

    private final List<CommandPipeline> commandPipelineList;

    public CommandPipeline find(Command command) {
        return commandPipelineList.stream()
                .filter(p -> p.support(command))
                .findFirst()
                .orElseThrow();
    }
}
