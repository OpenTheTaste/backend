package com.ott.api_user.playback.buffer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PlaybackCommandQueue {

    private final BlockingQueue<PlaybackCommand> queue;
    private final ConcurrentHashMap<PlaybackKey, PlaybackCommand> overflowMap = new ConcurrentHashMap<>();

    public PlaybackCommandQueue(
            @Value("${playback.buffer.queue-capacity:100000}") int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * API thread가 호출. non-blocking.
     * @return true=queue에 들어감, false=queue full
     */
    public boolean offer(long memberId, long contentsId, int positionSec) {
        PlaybackCommand command = new PlaybackCommand(
            memberId, contentsId, positionSec, Instant.now()
        );
        return queue.offer(command);
    }

    /**
     * Queue full 시 overflow map에 key별 최신값만 유지.
     * ConcurrentHashMap이므로 API thread 간 경합에 안전.
     */
    public void offerToOverflow(long memberId, long contentsId, int positionSec) {
        PlaybackCommand command = new PlaybackCommand(
            memberId, contentsId, positionSec, Instant.now()
        );
        overflowMap.merge(command.key(), command, (old, incoming) ->
            incoming.requestedAt().isAfter(old.requestedAt()) ? incoming : old);
    }

    /**
     * bulkSize 도달 시 혹은 timeout까지 모아서 반환.
     */
    public List<PlaybackCommand> drain(int bulkSize, long timeoutMs) throws InterruptedException {
        List<PlaybackCommand> batch = new ArrayList<>(bulkSize);
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (batch.size() < bulkSize) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) break;

            PlaybackCommand cmd = queue.poll(remaining, TimeUnit.MILLISECONDS);
            if (cmd == null) break;

            batch.add(cmd);
            queue.drainTo(batch, bulkSize - batch.size());
        }

        return batch;
    }

    /**
     * Worker가 flush 시 overflow map을 비우고 반환.
     */
    public Map<PlaybackKey, PlaybackCommand> drainOverflow() {
        if (overflowMap.isEmpty()) {
            return Map.of();
        }
        Map<PlaybackKey, PlaybackCommand> snapshot = new ConcurrentHashMap<>(overflowMap);
        overflowMap.keySet().removeAll(snapshot.keySet());
        return snapshot;
    }

    public int size() {
        return queue.size();
    }

    public int overflowSize() {
        return overflowMap.size();
    }
}
