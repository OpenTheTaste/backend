package com.ott.transcoder.config;

import com.ott.transcoder.exception.retryable.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.util.ErrorHandler;

@RequiredArgsConstructor
public class TranscodeErrorHandler implements ErrorHandler {

    private final FatalExceptionStrategy fatalExceptionStrategy;

    @Override
    public void handleError(Throwable t) {
        if (this.fatalExceptionStrategy.isFatal(t) && t instanceof ListenerExecutionFailedException)
            throw new ImmediateAcknowledgeAmqpException("[Fatal Error Detected]: " + t.getMessage(), t);
//        if (t instanceof RetryableException)
        throw new AmqpRejectAndDontRequeueException("[Retryable Error Detected]: " + t.getMessage(), t);
    }
}
