package com.medimitra.common.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medimitra.common.core.Region;
import com.medimitra.common.messaging.TopicNaming;
import com.medimitra.common.messaging.outbox.OutboxEvent;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Wraps {@code KafkaTemplate} so {@code OutboxRelay} never touches the Kafka API directly
 * (Adapter). Sends are keyed by {@code aggregateId}, so every event for one aggregate stays in
 * order; the producer factory backing this template is configured for idempotence and
 * {@code acks=all} (see {@code MediMitraMessagingAutoConfiguration}).
 */
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(OutboxEvent event) {
        String topic = TopicNaming.topic(
                Region.valueOf(event.getRegion()), event.getAggregateType(), event.getEventType(), event.getSchemaVersion());
        String value = toEnvelopeJson(event);
        try {
            kafkaTemplate.send(topic, event.getAggregateId(), value).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while publishing " + event.getEventType(), e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("failed to publish " + event.getEventType(), e.getCause());
        }
    }

    private String toEnvelopeJson(OutboxEvent event) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put("eventId", event.getEventId().toString());
            envelope.put("eventType", event.getEventType());
            envelope.put("schemaVersion", event.getSchemaVersion());
            envelope.put("aggregateType", event.getAggregateType());
            envelope.put("aggregateId", event.getAggregateId());
            envelope.put("region", event.getRegion());
            envelope.put("correlationId", event.getCorrelationId());
            envelope.put("causationId", event.getCausationId());
            envelope.put("occurredAt", event.getOccurredAt().toString());
            envelope.put("producer", event.getProducer());
            envelope.set("payload", objectMapper.readTree(event.getPayload()));
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("failed to build envelope JSON for " + event.getEventType(), e);
        }
    }
}
