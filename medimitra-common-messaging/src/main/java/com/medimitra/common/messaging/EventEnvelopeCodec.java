package com.medimitra.common.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.Region;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Decodes the wire-format JSON {@link com.medimitra.common.messaging.kafka.KafkaEventPublisher}
 * puts on the topic back into an {@link EventEnvelope}. Every consumer needs this same decoding —
 * it belongs here once, not reimplemented per service.
 */
public class EventEnvelopeCodec {

    private final ObjectMapper objectMapper;

    public EventEnvelopeCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> EventEnvelope<T> decode(String json, Class<T> payloadType) {
        try {
            JsonNode root = objectMapper.readTree(json);
            T payload = objectMapper.treeToValue(root.get("payload"), payloadType);
            return new EventEnvelope<>(
                    UUID.fromString(root.get("eventId").asText()),
                    root.get("eventType").asText(),
                    root.get("schemaVersion").asInt(),
                    root.get("aggregateType").asText(),
                    root.get("aggregateId").asText(),
                    Region.valueOf(root.get("region").asText()),
                    textOrNull(root.get("correlationId")),
                    textOrNull(root.get("causationId")),
                    Instant.parse(root.get("occurredAt").asText()),
                    root.get("producer").asText(),
                    payload);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("failed to decode event envelope", e);
        }
    }

    private static String textOrNull(JsonNode node) {
        return (node == null || node.isNull()) ? null : node.asText();
    }
}
