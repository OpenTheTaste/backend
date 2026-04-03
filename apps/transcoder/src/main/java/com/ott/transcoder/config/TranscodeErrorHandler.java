package com.ott.transcoder.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.util.ErrorHandler;

@Slf4j
@RequiredArgsConstructor
public class TranscodeErrorHandler implements ErrorHandler {

    private final FatalExceptionStrategy fatalExceptionStrategy;

    @Override
    public void handleError(Throwable t) {
        if (this.fatalExceptionStrategy.isFatal(t)) {
            log.error("[Fatal] 재시도 불가 → DLQ 이동: {}", t.getMessage(), t);
        } else {
            log.warn("[Retryable] 재시도 소진 → DLQ 이동: {}", t.getMessage(), t);
        }

        throw new AmqpRejectAndDontRequeueException("메시지 처리 실패 → DLQ 이동: " + t.getMessage(), t);
    }
}
