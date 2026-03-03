package com.ott.transcoder.config;

import com.ott.transcoder.exception.fatal.FatalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;

/**
 * [재시도 차단 전략 클래스]
 * FatalException은 아무리 다시 시도해도 해결되지 않으므로, 즉시 실패 큐로 보냄
 */
@Slf4j
public class TranscodeFatalExceptionStrategy implements FatalExceptionStrategy {

    @Override
    public boolean isFatal(Throwable t) {
        // 원인 예외(Cause)가 FatalException이거나 그 자식이면 '치명적(Fatal)'으로 간주
        Throwable cause = t.getCause();
        boolean isFatal = cause instanceof FatalException;
        if (isFatal) {
            log.error("[Fatal Error Detected] 재시도를 중단하고 실패 큐(DLQ)로 전송합니다: {}", cause.getMessage());
        }
        return isFatal;
    }
}
