package com.ott.transcoder.pipeline.hls;

import com.ott.transcoder.command.Command;
import com.ott.transcoder.command.CommandType;
import com.ott.transcoder.command.TranscodeCommand;
import com.ott.transcoder.job.JobContext;
import com.ott.transcoder.pipeline.CommandPipeline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TranscodeCommandPipeline implements CommandPipeline<TranscodeCommand> {

    @Override
    public boolean support(Command command) {
        return CommandType.TRANSCODE.equals(command.getType());
    }

    @Override
    public void execute(TranscodeCommand command, JobContext jobContext) {
        // TODO:
    }
}
