package com.ott.transcoder.queue;

import com.ott.infra.mq.TranscodeMessage;

/**
 * 메시지 큐 소비자 추상화 인터페이스
 *
 * 현재 구현체: RabbitTranscodeListener (RabbitMQ)
 * 큐 교체 시(SQS 등) 새 구현체만 추가하면 된다.
 */
public interface MessageListener {

    void listen(TranscodeMessage message);
}
