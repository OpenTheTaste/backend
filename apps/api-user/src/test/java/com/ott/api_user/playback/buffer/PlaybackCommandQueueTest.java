package com.ott.api_user.playback.buffer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlaybackCommandQueueTest {

    @Test
    void offer_returnsTrueWhenCapacityAvailable() {
        PlaybackCommandQueue queue = new PlaybackCommandQueue(10);
        assertThat(queue.offer(1L, 1L, 100)).isTrue();
    }

    @Test
    void offer_returnsFalseWhenQueueFull() {
        PlaybackCommandQueue queue = new PlaybackCommandQueue(1);
        queue.offer(1L, 1L, 100);
        assertThat(queue.offer(2L, 2L, 200)).isFalse();
    }

    @Test
    void drain_returnsEmptyWhenQueueEmptyAfterTimeout() throws InterruptedException {
        PlaybackCommandQueue queue = new PlaybackCommandQueue(10);
        List<PlaybackCommand> result = queue.drain(10, 50);
        assertThat(result).isEmpty();
    }

    @Test
    void drain_returnsBatchUpToBulkSize() throws InterruptedException {
        PlaybackCommandQueue queue = new PlaybackCommandQueue(100);
        for (int i = 0; i < 5; i++) {
            queue.offer(1L, (long) i, i * 10);
        }
        List<PlaybackCommand> result = queue.drain(3, 100);
        assertThat(result).hasSize(3);
    }

    @Test
    void offerToOverflow_keepsLatestByRequestedAt() {
        PlaybackCommandQueue queue = new PlaybackCommandQueue(1);
        queue.offerToOverflow(1L, 1L, 100);
        queue.offerToOverflow(1L, 1L, 200);

        Map<PlaybackKey, PlaybackCommand> overflow = queue.drainOverflow();
        assertThat(overflow).hasSize(1);
        assertThat(overflow.get(new PlaybackKey(1L, 1L)).positionSec()).isEqualTo(200);
    }

    @Test
    void drainOverflow_returnsEmptyMapWhenNoOverflow() {
        PlaybackCommandQueue queue = new PlaybackCommandQueue(10);
        assertThat(queue.drainOverflow()).isEmpty();
    }
}
