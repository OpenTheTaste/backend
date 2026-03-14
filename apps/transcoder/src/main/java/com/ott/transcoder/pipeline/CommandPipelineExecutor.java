package com.ott.transcoder.pipeline;

import com.ott.transcoder.command.Command;
import com.ott.transcoder.job.JobContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommandPipelineExecutor {

    private final CommandPipelineFactory factory;

    public String execute(Command command, JobContext jobContext) {
        CommandPipeline<?> pipeline = factory.find(command);
        return invoke(pipeline, command, jobContext);
    }

    @SuppressWarnings("unchecked")
    private <T extends Command> String invoke(CommandPipeline<T> pipeline, Command command, JobContext jobContext) {
        return pipeline.execute((T) command, jobContext);
    }
}
