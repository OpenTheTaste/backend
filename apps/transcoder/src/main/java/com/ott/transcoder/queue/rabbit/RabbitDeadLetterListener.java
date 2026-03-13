package com.ott.transcoder.queue.rabbit;

import com.ott.transcoder.config.RabbitConfig;
import com.ott.transcoder.job.IngestJobStatusManager;
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
public class RabbitDeadLetterListener {

    private final IngestJobStatusManager statusManager;

    @RabbitListener(queues = RabbitConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(TranscodeMessage message) {
        log.error("DLQ 수신 - mediaId: {}, ingestJobId: {}",
                message.mediaId(), message.ingestJobId());

        statusManager.fail(message.ingestJobId());

        log.info("실패 처리 완료 - ingestJobId: {}", message.ingestJobId());
    }
}
