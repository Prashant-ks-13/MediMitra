package com.medimitra.common.web.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InMemoryIdempotencyStoreTest {

    @Test
    void roundTripsARecord() {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        IdempotencyRecord record = new IdempotencyRecord("key-1", "fingerprint-1", 201, "application/json", "{}".getBytes(), Instant.now());

        store.save(record);

        assertTrue(store.find("key-1").isPresent());
        assertEquals(record, store.find("key-1").get());
        assertTrue(store.find("missing").isEmpty());
    }
}
