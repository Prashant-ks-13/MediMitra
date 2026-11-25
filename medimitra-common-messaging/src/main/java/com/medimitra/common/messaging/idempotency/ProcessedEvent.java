package com.medimitra.common.messaging.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** One row per (event, consumer) pair that has already been handled; existence alone marks a delivery as a duplicate. */
@Entity
@Table(name = "processed_event")
public class ProcessedEvent {

    @EmbeddedId
    private ProcessedEventId id;

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
        // JPA
    }

    public ProcessedEvent(ProcessedEventId id, Instant processedAt) {
        this.id = id;
        this.processedAt = processedAt;
    }

    public static ProcessedEvent of(UUID eventId, String consumerName, Instant processedAt) {
        return new ProcessedEvent(new ProcessedEventId(eventId, consumerName), processedAt);
    }

    public ProcessedEventId getId() {
        return id;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
