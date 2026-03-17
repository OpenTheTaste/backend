package com.ott.transcoder.queue.rabbit;

import com.ott.transcoder.config.RabbitConsumerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ListenerContainerIdleEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transcoder.messaging.rabbit.idle-exit-enabled", havingValue = "true")
public class RabbitIdleShutdownListener {

    private final ApplicationContext applicationContext;
    private final AtomicBoolean shutdownTriggered = new AtomicBoolean(false);

    @Value("${transcoder.messaging.rabbit.idle-exit-seconds:60}")
    private long idleExitSeconds;

    @EventListener
    public void onListenerIdle(ListenerContainerIdleEvent event) {
        if (!RabbitConsumerConfig.LISTENER_ID.equals(event.getListenerId())) {
            return;
        }
        if (!shutdownTriggered.compareAndSet(false, true)) {
            return;
        }

        log.info("no transcode message for {}s. shutting down worker (listenerId={})",
                idleExitSeconds, event.getListenerId());

        Thread shutdownThread = new Thread(() -> {
            int exitCode = SpringApplication.exit(applicationContext, () -> 0);
            System.exit(exitCode);
        }, "rabbit-idle-shutdown");
        shutdownThread.setDaemon(false);
        shutdownThread.start();
    }
}
