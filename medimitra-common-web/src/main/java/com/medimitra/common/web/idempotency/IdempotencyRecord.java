package com.medimitra.common.web.idempotency;

import java.time.Instant;

/** A stored first response for one Idempotency-Key, keyed also by a fingerprint of the request that produced it. */
public record IdempotencyRecord(String key, String fingerprint, int status, String contentType, byte[] body, Instant storedAt) {
}
