package com.ott.transcoder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ott.transcoder.exception.fatal.FatalException;
import com.ott.transcoder.queue.TranscodeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 
 * 비즈니스 로직에서 발생하는 예외의 성격(Fatal vs Retryable)을 판단하여, 자동으로 재시도하거나 실패 큐(DLQ)로 격리
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "transcoder.messaging.provider", havingValue = "rabbit")
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "transcode.exchange";
    public static final String QUEUE_NAME = "transcode.queue";
    public static final String ROUTING_KEY = "transcode.request";

    public static final String DEAD_LETTER_EXCHANGE = "transcode.dead.exchange";
    public static final String DEAD_LETTER_QUEUE = "transcode.dead.queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "transcode.dead.key";

    @Bean
    public DirectExchange transcodeExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_NAME).durable(true).build();
    }

    /** 
     * 실패(Reject) 시 자동으로 실패 수용소(DLX)로 안내
     */
    @Bean
    public Queue transcodeQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Binding transcodeBinding(Queue transcodeQueue, DirectExchange transcodeExchange) {
        return BindingBuilder.bind(transcodeQueue)
                .to(transcodeExchange).
                with(ROUTING_KEY);
    }

    /** 예외를 분석하여 '재시도'할지 '즉시 포기'할지 결정 */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter jacksonMessageConverter
    ) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);

        // FatalException 발생 시 재시도 없이 즉시 거절(Reject) 처리
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(new TranscodeFatalExceptionStrategy()));

        // 일시적 장애(Retryable) 시 3회 재시도 (1s -> 2s -> 4s)
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer()) // 3번 다 실패하면 DLQ행
                .build());

        return factory;
    }

    /**
     * [재시도 차단 전략 클래스]
     * FatalException은 아무리 다시 시도해도 해결되지 않으므로, 즉시 실패 큐로 보냄
     */
    private static class TranscodeFatalExceptionStrategy implements FatalExceptionStrategy {
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

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setDefaultType(TranscodeMessage.class);
        return classMapper;
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper, DefaultClassMapper classMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setClassMapper(classMapper);
        return converter;
    }
}
