package com.medimitra.common.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A row written in the producer's own transaction; {@code OutboxRelay} is the only component
 * allowed to turn a row into a Kafka send, so a DB commit and its event can never diverge.
 */
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private int schemaVersion;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String region;

    private String correlationId;

    private String causationId;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private String producer;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    private Instant publishedAt;

    private int attemptCount;

    protected OutboxEvent() {
        // JPA
    }

    public OutboxEvent(
            UUID eventId,
            String eventType,
            int schemaVersion,
            String aggregateType,
            String aggregateId,
            String region,
            String correlationId,
            String causationId,
            Instant occurredAt,
            String producer,
            String payload) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.schemaVersion = schemaVersion;
        this.aggregateType = Objects.requireNonNull(aggregateType, "aggregateType");
        this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId");
        this.region = Objects.requireNonNull(region, "region");
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.producer = Objects.requireNonNull(producer, "producer");
        this.payload = Objects.requireNonNull(payload, "payload");
    }

    public void markPublished(Instant publishedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
    }

    /** After {@code maxAttempts} failed sends the row is parked as {@code FAILED} instead of retried forever. */
    public void recordFailedAttempt(int maxAttempts) {
        this.attemptCount++;
        this.status = (this.attemptCount >= maxAttempts) ? OutboxStatus.FAILED : OutboxStatus.PENDING;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getRegion() {
        return region;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getProducer() {
        return producer;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }
}
