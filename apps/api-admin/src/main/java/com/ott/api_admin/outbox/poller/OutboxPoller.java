package com.ott.api_admin.outbox.poller;

import com.ott.api_admin.outbox.writer.OutboxWriter;
import com.ott.api_admin.publish.RabbitTranscodePublisher;
import com.ott.domain.outbox.domain.OutboxStatus;
import com.ott.domain.outbox.domain.TranscodeOutbox;
import com.ott.domain.outbox.repository.TranscodeOutboxRepository;
import com.ott.infra.mq.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final TranscodeOutboxRepository outboxRepository;
    private final RabbitTranscodePublisher rabbitTranscodePublisher;
    private final OutboxWriter outboxWriter;

    /**
     * 10초마다 PENDING 상태의 Outbox 메시지를 폴링하여 발행한다.
     *
     * - 발행 성공 → PUBLISHED
     * - 발행 실패 → retryCount++ (maxRetries 초과 시 FAILED)
     */
    @Scheduled(fixedDelay = 10000)
    public void pollAndPublish() {
        List<TranscodeOutbox> pendingList =
                outboxRepository.findTop50ByOutboxStatusOrderByCreatedDateAsc(OutboxStatus.PENDING);

        if (pendingList.isEmpty()) {
            return;
        }

        log.info("Outbox 폴링 - {}건 발행 시도", pendingList.size());

        for (TranscodeOutbox transcodeOutbox : pendingList) {
            try {
                rabbitTranscodePublisher.publish(toMessage(transcodeOutbox));
                outboxWriter.markAsPublished(transcodeOutbox.getId());

                log.info("Outbox 발행 성공 - outboxId: {}, ingestJobId: {}",
                        transcodeOutbox.getId(), transcodeOutbox.getIngestJobId());

            } catch (Exception e) {
                outboxWriter.markAsFailed(transcodeOutbox.getId(), e.getMessage());

                log.warn("Outbox 발행 실패 - outboxId: {}, retryCount: {}/{}, error: {}",
                        transcodeOutbox.getId(), transcodeOutbox.getRetryCount(), transcodeOutbox.getMaxRetries(), e.getMessage());
            }
        }
    }

    private TranscodeMessage toMessage(TranscodeOutbox transcodeOutbox) {
        return new TranscodeMessage(
                transcodeOutbox.getMediaId(),
                transcodeOutbox.getIngestJobId(),
                transcodeOutbox.getOriginUrl(),
                transcodeOutbox.getFileSize(),
                transcodeOutbox.getMediaType()
        );
    }
}
