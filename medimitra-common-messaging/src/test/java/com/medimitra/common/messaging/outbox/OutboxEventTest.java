package com.medimitra.common.messaging.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxEventTest {

    @Test
    void staysPendingUntilMaxAttemptsReached() {
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(), "booked", 1, "appointment", "appt-1", "IN", null, null,
                Instant.now(), "scheduling", "{}");

        event.recordFailedAttempt(3);
        assertEquals(OutboxStatus.PENDING, event.getStatus());

        event.recordFailedAttempt(3);
        event.recordFailedAttempt(3);
        assertEquals(OutboxStatus.FAILED, event.getStatus());
        assertEquals(3, event.getAttemptCount());
    }

    @Test
    void marksPublished() {
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(), "booked", 1, "appointment", "appt-1", "IN", null, null,
                Instant.now(), "scheduling", "{}");
        Instant publishedAt = Instant.now();

        event.markPublished(publishedAt);

        assertEquals(OutboxStatus.PUBLISHED, event.getStatus());
        assertEquals(publishedAt, event.getPublishedAt());
    }
}
