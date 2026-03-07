package com.ott.transcoder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ott.transcoder.exception.fatal.FatalException;
import com.ott.transcoder.exception.retryable.RetryableException;
import com.ott.transcoder.queue.TranscodeMessage;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.util.ErrorHandler;

import java.util.Map;

/** 
 * 비즈니스 로직에서 발생하는 예외의 성격(Fatal vs Retryable)을 판단하여, 자동으로 재시도하거나 실패 큐(DLQ)로 격리
 */
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
//        factory.setErrorHandler(new ConditionalRejectingErrorHandler(new TranscodeFatalExceptionStrategy()));
        factory.setErrorHandler(errorHandler());

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
