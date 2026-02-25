package com.ott.transcoder.queue.rabbit;

import com.ott.transcoder.config.RabbitConfig;
import com.ott.transcoder.pipeline.CommandPipeline;
import com.ott.transcoder.queue.MessageListener;
import com.ott.transcoder.queue.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 메시지 리스너 (어댑터 역할)
 *
 * 큐에서 메시지를 소비하여 CommandPipeline에 위임
 * RabbitMQ 전용 로직만 담당, 트랜스코딩 비즈니스 로직은 모르는 상태
 *
 * SQS 전환 시 이 클래스 대신 SqsTranscodeListener를 만들면 된다
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transcoder.messaging.provider", havingValue = "rabbit")
public class RabbitTranscodeListener implements MessageListener {

    private final CommandPipeline commandPipeline;

    @Override
    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void listen(TranscodeMessage message) {
        log.info("트랜스코딩 요청 수신 - mediaId: {}, originUrl: {}", message.mediaId(), message.originUrl());
        commandPipeline.execute(message.mediaId(), message.originUrl());
    }
}
