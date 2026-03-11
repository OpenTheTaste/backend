package com.ott.transcoder.pipeline;

import com.ott.transcoder.command.Command;
import com.ott.transcoder.job.JobContext;

/**
 * 커맨드별 미디어 처리 파이프라인
 * 구현체는 미디어 처리 자체에만 집중
 */
public interface CommandPipeline<T extends Command> {

   boolean support(Command command);

    void execute(T command, JobContext jobContext);
}
