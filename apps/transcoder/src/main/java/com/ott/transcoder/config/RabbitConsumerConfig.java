package com.ott.transcoder.config;

import com.ott.infra.mq.TranscodeConstants;
import com.ott.transcoder.exception.fatal.FatalException;
import com.ott.transcoder.exception.retryable.RetryableException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.util.ErrorHandler;

import java.util.Map;

/**
 * 트랜스코더 소비자 전용 RabbitMQ 설정.
 * Queue/Binding/DLQ 선언, ListenerContainerFactory, 재시도 정책, 에러 핸들러를 관리한다.
 * 공유 설정(Exchange/RoutingKey 상수, MessageConverter)은 infra-mq 모듈에서 제공한다.
 */
@Configuration
@ConditionalOnProperty(name = "transcoder.messaging.provider", havingValue = "rabbit")
public class RabbitConsumerConfig {

    public static final String QUEUE_NAME = "transcode.queue";
    public static final String LISTENER_ID = "transcode-consumer";

    public static final String DEAD_LETTER_EXCHANGE = "transcode.dead.exchange";
    public static final String DEAD_LETTER_QUEUE = "transcode.dead.queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "transcode.dead.key";

    @Bean
    public DirectExchange transcodeExchange() {
        return ExchangeBuilder.directExchange(TranscodeConstants.EXCHANGE_NAME).durable(true).build();
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
                .to(transcodeExchange)
                .with(TranscodeConstants.ROUTING_KEY);
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

        factory.setErrorHandler(errorHandler());
        factory.setPrefetchCount(1);

        // 일시적 장애(Retryable) 시 3회 재시도 (1s -> 2s -> 4s)
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .retryPolicy(new SimpleRetryPolicy(3, Map.of(RetryableException.class, true, FatalException.class, false), true))
                .backOffOptions(1000, 2.0, 10000)
                .build());

        return factory;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new TranscodeErrorHandler(fatalExceptionStrategy());
    }

    @Bean
    public FatalExceptionStrategy fatalExceptionStrategy() {
        return new TranscodeFatalExceptionStrategy();
    }
}
