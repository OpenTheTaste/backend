package com.ott.transcoder.pipeline;

import com.ott.transcoder.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommandPipelineExecutor {

    private final CommandPipelineFactory factory;

    public void execute(Command command) {
        CommandPipeline<?> pipeline = factory.find(command);
        invoke(pipeline, command);
//        pipeline.execute(command);
    }

    @SuppressWarnings("unchecked")
    private <T extends Command> void invoke(CommandPipeline<T> pipeline, Command command) {
        // 여기서 (T) 캐스팅은 팩토리가 이미 검증했으므로
        // 자바가 타입을 딱 맞춰줘서 파이프라인의 execute(T command)가 바로 호출
        pipeline.execute((T) command);
    }
}
