package com.ott.transcoder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ott.transcoder.queue.TranscodeMessage;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정.
 *
 * Exchange → Binding → Queue 구조로 메시지 라우팅
 * Producer가 transcode.exchange에 routing key "transcode.request"로 메시지를 발행하면,
 * Binding을 통해 transcode.queue로 전달되고, RabbitTranscodeListener가 소비
 *
 * transcoder.messaging.provider=rabbit 일 때만 활성화 (SQS 등 전환 시 비활성화)
 */
@Configuration
@ConditionalOnProperty(name = "transcoder.messaging.provider", havingValue = "rabbit")
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "transcode.exchange";
    public static final String QUEUE_NAME = "transcode.queue";
    public static final String ROUTING_KEY = "transcode.request";

    @Bean
    public DirectExchange transcodeExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    /** durable=true: RabbitMQ 재시작 시에도 큐 유지 */
    @Bean
    public Queue transcodeQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    /** Exchange와 Queue를 routing key로 연결 */
    @Bean
    public Binding transcodeBinding(Queue transcodeQueue, DirectExchange transcodeExchange) {
        return BindingBuilder.bind(transcodeQueue)
                .to(transcodeExchange)
                .with(ROUTING_KEY);
    }

    /**
     * JSON 메시지를 TranscodeMessage로 역직렬화할 때 사용할 기본 타입 지정
     * 메시지 헤더에 __TypeId__가 없어도 TranscodeMessage로 변환
     * (Management UI 등 외부에서 직접 발행한 메시지 처리를 위해 필요)
     */
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
