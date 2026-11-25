package com.medimitra.common.messaging.idempotency;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.messaging.EventEnvelope;
import org.springframework.transaction.annotation.Transactional;

/** Template Method: every consumer gets dedupe, validation and recording for free (README section 7.4). */
public abstract class AbstractIdempotentConsumer<T> {

    private final ProcessedEventRepository processed;
    private final ClockProvider clockProvider;

    protected AbstractIdempotentConsumer(ProcessedEventRepository processed, ClockProvider clockProvider) {
        this.processed = processed;
        this.clockProvider = clockProvider;
    }

    @Transactional
    public final void consume(EventEnvelope<T> event) {
        ProcessedEventId id = new ProcessedEventId(event.eventId(), consumerName());
        if (processed.existsById(id)) {
            return; // duplicate delivery
        }
        validate(event);
        handle(event);
        processed.save(ProcessedEvent.of(event.eventId(), consumerName(), clockProvider.now()));
    }

    protected void validate(EventEnvelope<T> event) {
        // no-op by default; override for schema or business validation
    }

    protected abstract void handle(EventEnvelope<T> event);

    protected abstract String consumerName();
}
