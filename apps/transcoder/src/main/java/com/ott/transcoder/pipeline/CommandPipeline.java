package com.ott.transcoder.pipeline;

import com.ott.transcoder.command.Command;
import com.ott.transcoder.job.JobContext;

/**
 * 커맨드별 미디어 처리 파이프라인
 * 구현체는 미디어 처리 + 산출물 업로드까지 담당
 *
 * @return 업로드된 산출물의 S3 key (outputUrl)
 */
public interface CommandPipeline<T extends Command> {

   boolean support(Command command);

    String execute(T command, JobContext jobContext);
}
