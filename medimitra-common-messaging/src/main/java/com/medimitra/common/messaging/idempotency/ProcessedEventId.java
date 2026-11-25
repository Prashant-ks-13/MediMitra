package com.medimitra.common.messaging.idempotency;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Dedupe key is (eventId, consumerName), not eventId alone, so distinct consumer groups each process an event once. */
@Embeddable
public class ProcessedEventId implements Serializable {

    private UUID eventId;
    private String consumerName;

    protected ProcessedEventId() {
        // JPA
    }

    public ProcessedEventId(UUID eventId, String consumerName) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.consumerName = Objects.requireNonNull(consumerName, "consumerName");
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getConsumerName() {
        return consumerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessedEventId other)) {
            return false;
        }
        return eventId.equals(other.eventId) && consumerName.equals(other.consumerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, consumerName);
    }
}
