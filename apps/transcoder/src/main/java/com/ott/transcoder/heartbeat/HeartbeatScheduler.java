package com.ott.transcoder.heartbeat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ott.transcoder.constant.IngestJobConstant.HeartbeatConstant.HEARTBEAT_INTERVAL_SEC;

/**
 * Heartbeat 스케줄러 시작/종료를 관리한다.
 * start()로 주기적 갱신을 시작하고, 반환된 Heartbeat를 close()하면 갱신이 중단된다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class HeartbeatScheduler {

    private final HeartbeatWriter heartbeatWriter;

    /**
     * Heartbeat 주기적 갱신을 시작한다.
     * CAS 선점 시 이미 heartbeat_at = NOW()가 세팅되었으므로, 초기 지연 = INTERVAL로 설정한다.
     *
     * @return Heartbeat (AutoCloseable) - try-with-resources로 사용
     */
    public Heartbeat start(Long jobId) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                heartbeatWriter.updateHeartbeat(jobId);
            } catch (Exception e) {
                log.warn("Heartbeat 갱신 실패 - jobId: {} (1~2회 누락은 TIMEOUT이 허용)", jobId, e);
            }
        }, HEARTBEAT_INTERVAL_SEC, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);

        return new Heartbeat(scheduler);
    }
}
