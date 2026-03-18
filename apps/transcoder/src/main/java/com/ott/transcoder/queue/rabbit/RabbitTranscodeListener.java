package com.ott.transcoder.queue.rabbit;

import com.ott.transcoder.job.JobOrchestrator;
import com.ott.transcoder.config.RabbitConsumerConfig;
import com.ott.transcoder.queue.MessageListener;
import com.ott.infra.mq.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transcoder.messaging.provider", havingValue = "rabbit")
public class RabbitTranscodeListener implements MessageListener {

    private final JobOrchestrator jobOrchestrator;
    private final Optional<AsgScaleInProtectionManager> scaleInProtectionManager;

    @Override
    @RabbitListener(id = RabbitConsumerConfig.LISTENER_ID, queues = RabbitConsumerConfig.QUEUE_NAME)
    public void listen(TranscodeMessage message) {
        log.info("작업 요청 수신 - mediaId: {}, originUrl: {}", message.mediaId(), message.originUrl());

        scaleInProtectionManager.ifPresent(AsgScaleInProtectionManager::markBusy);
        try {
            jobOrchestrator.handle(message);
        } finally {
            scaleInProtectionManager.ifPresent(AsgScaleInProtectionManager::markIdle);
        }

        log.info("작업 요청 처리 완료 - mediaId: {}, originUrl: {}", message.mediaId(), message.originUrl());
    }
}
