package com.ott.api_user.media_metrics.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaMetricsBatchScheduler {

    private final MetricsCalculator metricsCalculator;

    @Scheduled(cron = "0 0 */6 * * *")
    public void run() {
        log.info("[MediaMetricsBatch] 스케줄 트리거");
        metricsCalculator.calculate();
    }
}
