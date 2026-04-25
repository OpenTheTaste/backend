package com.ott.transcoder.heartbeat;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Heartbeat 스케줄러의 생명주기를 관리하는 AutoCloseable 래퍼.
 * try-with-resources로 사용하여 작업 종료 시 자동으로 스케줄러를 정리한다.
 */
public class Heartbeat implements AutoCloseable {

    private final ScheduledExecutorService scheduler;

    public Heartbeat(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
