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

/** RabbitMQ Exchange/Queue/Binding 설정. transcoder.messaging.provider=rabbit 일 때 활성화. */
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

    /** __TypeId__ 헤더 없는 메시지도 역직렬화 가능하도록 기본 타입 지정 */
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
