package com.medimitra.common.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.Region;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventEnvelopeCodecTest {

    private record SamplePayload(String foo, int bar) {
    }

    @Test
    void decodesTheExactWireFormatKafkaEventPublisherProduces() {
        ObjectMapper objectMapper = new ObjectMapper();
        EventEnvelopeCodec codec = new EventEnvelopeCodec(objectMapper);
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-01-01T00:00:00Z");

        String json = "{"
                + "\"eventId\":\"" + eventId + "\","
                + "\"eventType\":\"registered\","
                + "\"schemaVersion\":1,"
                + "\"aggregateType\":\"user\","
                + "\"aggregateId\":\"agg-1\","
                + "\"region\":\"IN\","
                + "\"correlationId\":null,"
                + "\"causationId\":null,"
                + "\"occurredAt\":\"" + occurredAt + "\","
                + "\"producer\":\"identity-access\","
                + "\"payload\":{\"foo\":\"hello\",\"bar\":42}"
                + "}";

        EventEnvelope<SamplePayload> decoded = codec.decode(json, SamplePayload.class);

        assertEquals(eventId, decoded.eventId());
        assertEquals("registered", decoded.eventType());
        assertEquals(1, decoded.schemaVersion());
        assertEquals("user", decoded.aggregateType());
        assertEquals("agg-1", decoded.aggregateId());
        assertEquals(Region.IN, decoded.region());
        assertNull(decoded.correlationId());
        assertNull(decoded.causationId());
        assertEquals(occurredAt, decoded.occurredAt());
        assertEquals("identity-access", decoded.producer());
        assertEquals(new SamplePayload("hello", 42), decoded.payload());
    }
}
