package com.medimitra.common.messaging;

import com.medimitra.common.core.Region;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable envelope wrapping every event on the Kafka bus. {@code eventType} is the short action
 * name (e.g. {@code "booked"}), {@code aggregateType} the owning aggregate (e.g. {@code "appointment"});
 * {@link TopicNaming} combines them with {@code schemaVersion} into the actual topic name.
 *
 * <p>{@code eventId} and {@code occurredAt} are required inputs, not generated here, so producers
 * supply them from their own injected {@code IdGenerator}/{@code ClockProvider} and stay testable.
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int schemaVersion,
        String aggregateType,
        String aggregateId,
        Region region,
        String correlationId,
        String causationId,
        Instant occurredAt,
        String producer,
        T payload) {

    public EventEnvelope {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(aggregateType, "aggregateType");
        Objects.requireNonNull(aggregateId, "aggregateId");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(producer, "producer");
        Objects.requireNonNull(payload, "payload");
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public Builder<T> toBuilder() {
        return new Builder<T>()
                .eventId(eventId)
                .eventType(eventType)
                .schemaVersion(schemaVersion)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .region(region)
                .correlationId(correlationId)
                .causationId(causationId)
                .occurredAt(occurredAt)
                .producer(producer)
                .payload(payload);
    }

    /** Many optional fields (correlationId, causationId); the built envelope is always immutable. */
    public static final class Builder<T> {
        private UUID eventId;
        private String eventType;
        private int schemaVersion = 1;
        private String aggregateType;
        private String aggregateId;
        private Region region;
        private String correlationId;
        private String causationId;
        private Instant occurredAt;
        private String producer;
        private T payload;

        private Builder() {
        }

        public Builder<T> eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder<T> eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder<T> schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        public Builder<T> aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder<T> aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder<T> region(Region region) {
            this.region = region;
            return this;
        }

        public Builder<T> correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder<T> causationId(String causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder<T> occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Builder<T> producer(String producer) {
            this.producer = producer;
            return this;
        }

        public Builder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public EventEnvelope<T> build() {
            return new EventEnvelope<>(eventId, eventType, schemaVersion, aggregateType, aggregateId,
                    region, correlationId, causationId, occurredAt, producer, payload);
        }
    }
}
