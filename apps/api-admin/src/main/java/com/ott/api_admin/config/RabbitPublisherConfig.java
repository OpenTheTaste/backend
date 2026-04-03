package com.ott.api_admin.config;

import com.ott.infra.mq.TranscodeConstants;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPublisherConfig {

    @Bean
    public DirectExchange transcodeExchange() {
        return ExchangeBuilder
                .directExchange(TranscodeConstants.EXCHANGE_NAME)
                .durable(true)
                .build();
    }
}
