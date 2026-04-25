package com.ott.api_admin.publish;

import com.ott.infra.mq.TranscodeConstants;
import com.ott.infra.mq.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitTranscodePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(TranscodeMessage message) {
        rabbitTemplate.invoke(operations -> {
            operations.convertAndSend(
                    TranscodeConstants.EXCHANGE_NAME,
                    TranscodeConstants.ROUTING_KEY,
                    message
            );
            operations.waitForConfirmsOrDie(5_000);
            return null;
        });
        log.info("트랜스코딩 요청 발행 확인 - mediaId: {}, ingestJobId: {}",
                message.mediaId(), message.ingestJobId());
    }
}
