package com.medimitra.common.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.medimitra.common.core.Region;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventEnvelopeTest {

    @Test
    void buildsWithDefaultSchemaVersionOne() {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        EventEnvelope<String> event = EventEnvelope.<String>builder()
                .eventId(eventId)
                .eventType("booked")
                .aggregateType("appointment")
                .aggregateId("appt-1")
                .region(Region.IN)
                .occurredAt(now)
                .producer("scheduling")
                .payload("payload")
                .build();

        assertEquals(1, event.schemaVersion());
        assertEquals(eventId, event.eventId());
    }

    @Test
    void toBuilderCopiesEveryField() {
        EventEnvelope<String> original = EventEnvelope.<String>builder()
                .eventId(UUID.randomUUID())
                .eventType("booked")
                .aggregateType("appointment")
                .aggregateId("appt-1")
                .region(Region.EU)
                .correlationId("corr-1")
                .occurredAt(Instant.now())
                .producer("scheduling")
                .payload("payload")
                .build();

        EventEnvelope<String> copy = original.toBuilder().correlationId("corr-2").build();

        assertEquals(original.eventId(), copy.eventId());
        assertEquals("corr-2", copy.correlationId());
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThrows(NullPointerException.class, () -> EventEnvelope.<String>builder()
                .eventType("booked")
                .aggregateType("appointment")
                .aggregateId("appt-1")
                .region(Region.US)
                .occurredAt(Instant.now())
                .producer("scheduling")
                .payload("payload")
                .build());
    }
}
