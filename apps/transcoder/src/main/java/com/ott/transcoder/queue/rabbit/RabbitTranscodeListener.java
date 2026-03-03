package com.ott.transcoder.queue.rabbit;

import com.ott.transcoder.JobOrchestrator;
import com.ott.transcoder.config.RabbitConfig;
import com.ott.transcoder.queue.MessageListener;
import com.ott.transcoder.queue.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transcoder.messaging.provider", havingValue = "rabbit")
public class RabbitTranscodeListener implements MessageListener {

    private final JobOrchestrator jobOrchestrator;

    @Override
    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void listen(TranscodeMessage message) throws Exception {
        log.info("작업 요청 수신 - mediaId: {}, originUrl: {}", message.mediaId(), message.originUrl());

        jobOrchestrator.handle(message);

        log.info("작업 요청 처리 완료 - mediaId: {}, originUrl: {}", message.mediaId(), message.originUrl());
    }
}
