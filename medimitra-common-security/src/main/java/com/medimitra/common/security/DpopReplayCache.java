package com.medimitra.common.security;

import java.time.Instant;

/** Backs DPoP {@code jti} replay detection (RFC 9449); production should wire a Redis-backed bean. */
public interface DpopReplayCache {
    boolean isReplayed(String jti);

    void remember(String jti, Instant expiresAt);
}
