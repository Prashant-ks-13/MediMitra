package com.medimitra.common.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventPublisher;
import java.io.UncheckedIOException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes every event to the outbox table in the caller's own transaction; {@link OutboxRelay}
 * publishes it to Kafka afterwards. Propagation is {@code MANDATORY}: publishing outside a
 * transaction the caller already opened is a startup-time wiring bug, not a silently dropped event.
 */
public class OutboxEventPublisher implements EventPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(EventEnvelope<?> event) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(event.payload());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("failed to serialise payload for event type " + event.eventType(), e);
        }
        repository.save(new OutboxEvent(
                event.eventId(),
                event.eventType(),
                event.schemaVersion(),
                event.aggregateType(),
                event.aggregateId(),
                event.region().name(),
                event.correlationId(),
                event.causationId(),
                event.occurredAt(),
                event.producer(),
                payloadJson));
    }
}
