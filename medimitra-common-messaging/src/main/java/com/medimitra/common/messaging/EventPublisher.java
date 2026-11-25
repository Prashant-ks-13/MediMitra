package com.medimitra.common.messaging;

/**
 * Port every producer depends on instead of {@code KafkaTemplate} directly (dependency inversion).
 * The default implementation writes to the transactional outbox inside the caller's own
 * transaction, so a database commit and its event can never diverge.
 */
public interface EventPublisher {
    void publish(EventEnvelope<?> event);
}
