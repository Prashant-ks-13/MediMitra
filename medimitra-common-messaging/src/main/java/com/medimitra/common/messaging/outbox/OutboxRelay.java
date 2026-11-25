package com.medimitra.common.messaging.outbox;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.messaging.kafka.KafkaEventPublisher;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/** Polls the outbox and forwards pending rows to Kafka; the only component allowed to talk to the broker on the write path. */
public class OutboxRelay {

    private final OutboxEventRepository repository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ClockProvider clockProvider;
    private final int batchSize;
    private final int maxAttempts;

    public OutboxRelay(
            OutboxEventRepository repository,
            KafkaEventPublisher kafkaEventPublisher,
            ClockProvider clockProvider,
            int batchSize,
            int maxAttempts) {
        this.repository = repository;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.clockProvider = clockProvider;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${medimitra.messaging.relay.fixed-delay-ms:2000}")
    @Transactional
    public void dispatchPendingBatch() {
        List<OutboxEvent> batch = repository.lockNextBatch(batchSize);
        for (OutboxEvent event : batch) {
            try {
                kafkaEventPublisher.send(event);
                event.markPublished(clockProvider.now());
            } catch (RuntimeException e) {
                event.recordFailedAttempt(maxAttempts);
            }
        }
    }
}
