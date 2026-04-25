package com.ott.transcoder.queue.rabbit;

import com.ott.infra.mq.TranscodeMessage;
import com.ott.transcoder.config.RabbitConsumerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Delay Queue 발행 담당.
 * CAS 선점 실패 + 하트비트 유효(다른 워커 생존) 시,
 * x-delayed=true 헤더를 부착하여 Delay Queue로 메시지를 발행한다.
 * TTL 만료 후 메인 큐로 복귀하여 1회 재확인한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "transcoder.messaging.provider", havingValue = "rabbit")
public class DelayQueuePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishToDelay(TranscodeMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitConsumerConfig.DELAY_EXCHANGE,
                RabbitConsumerConfig.DELAY_ROUTING_KEY,
                message,
                msg -> {
                    msg.getMessageProperties().setHeader("x-delayed", true);
                    return msg;
                }
        );
        log.info("Delay Queue 발행 - ingestJobId: {}, mediaId: {}",
                message.ingestJobId(), message.mediaId());
    }
}
