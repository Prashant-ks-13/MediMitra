package com.medimitra.common.web.idempotency;

import java.util.Optional;

/** Backs {@code @Idempotent}; production should wire a Redis-backed bean for multi-instance correctness. */
public interface IdempotencyStore {
    Optional<IdempotencyRecord> find(String key);

    void save(IdempotencyRecord record);
}
