package com.medimitra.common.messaging.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.Region;
import com.medimitra.common.messaging.EventEnvelope;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AbstractIdempotentConsumerTest {

    @Test
    void handlesEachEventExactlyOncePerConsumer() {
        ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
        ClockProvider clockProvider = mock(ClockProvider.class);
        when(clockProvider.now()).thenReturn(Instant.now());
        when(repository.existsById(any())).thenReturn(false);

        AtomicInteger handledCount = new AtomicInteger();
        AbstractIdempotentConsumer<String> consumer = new AbstractIdempotentConsumer<>(repository, clockProvider) {
            @Override
            protected void handle(EventEnvelope<String> event) {
                handledCount.incrementAndGet();
            }

            @Override
            protected String consumerName() {
                return "test-consumer";
            }
        };

        EventEnvelope<String> event = EventEnvelope.<String>builder()
                .eventId(UUID.randomUUID())
                .eventType("booked")
                .aggregateType("appointment")
                .aggregateId("appt-1")
                .region(Region.IN)
                .occurredAt(Instant.now())
                .producer("scheduling")
                .payload("payload")
                .build();

        consumer.consume(event);

        assertEquals(1, handledCount.get());
        verify(repository).save(any());
    }

    @Test
    void skipsDuplicateDelivery() {
        ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
        ClockProvider clockProvider = mock(ClockProvider.class);
        when(repository.existsById(any())).thenReturn(true);

        AtomicInteger handledCount = new AtomicInteger();
        AbstractIdempotentConsumer<String> consumer = new AbstractIdempotentConsumer<>(repository, clockProvider) {
            @Override
            protected void handle(EventEnvelope<String> event) {
                handledCount.incrementAndGet();
            }

            @Override
            protected String consumerName() {
                return "test-consumer";
            }
        };

        EventEnvelope<String> event = EventEnvelope.<String>builder()
                .eventId(UUID.randomUUID())
                .eventType("booked")
                .aggregateType("appointment")
                .aggregateId("appt-1")
                .region(Region.IN)
                .occurredAt(Instant.now())
                .producer("scheduling")
                .payload("payload")
                .build();

        consumer.consume(event);

        assertEquals(0, handledCount.get());
    }
}
