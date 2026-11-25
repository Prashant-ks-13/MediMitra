package com.medimitra.common.web.idempotency;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Single-instance default; a multi-instance deployment needs a shared (Redis) implementation instead. */
public final class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Map<String, IdempotencyRecord> records = new ConcurrentHashMap<>();

    @Override
    public Optional<IdempotencyRecord> find(String key) {
        return Optional.ofNullable(records.get(key));
    }

    @Override
    public void save(IdempotencyRecord record) {
        records.put(record.key(), record);
    }
}
