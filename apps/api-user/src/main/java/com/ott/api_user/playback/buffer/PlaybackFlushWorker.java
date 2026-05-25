package com.ott.api_user.playback.buffer;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaybackFlushWorker {

    private final PlaybackCommandQueue commandQueue;
    private final PlaybackBatchRepository batchRepository;

    @Value("${playback.buffer.bulk-size:1000}")
    private int bulkSize;

    @Value("${playback.buffer.flush-timeout-ms:1000}")
    private long flushTimeoutMs;

    private volatile boolean running = true;
    private Thread leaderThread;

    @PostConstruct
    void init() {
        leaderThread = new Thread(this::leaderLoop, "playback-flush-leader");
        leaderThread.setDaemon(true);
        leaderThread.start();
    }

    private void leaderLoop() {
        while (running) {
            try {
                flush();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Flush failed", e);
            }
        }
    }

    private void flush() throws InterruptedException {
        List<PlaybackCommand> drained = commandQueue.drain(bulkSize, flushTimeoutMs);
        if (drained.isEmpty()) return;

        batchRepository.batchUpdate(drained);
    }

    @PreDestroy
    void shutdown() {
        running = false;
        leaderThread.interrupt();
        try {
            flush();
        } catch (Exception ignored) {
        }
    }
}
