package com.medimitra.common.security;

import java.time.Instant;

/**
 * Backs the {@code jti}/{@code sid} revocation check in section 8.3, rule 7, and logout's "add sid
 * to the denylist until the access token's expiry" rule (section 8.4). Production should wire a
 * Redis-backed bean; this port lets a service do that without touching {@link JwtTokenValidator}.
 */
public interface TokenDenylist {
    boolean isDenied(String jti, String sid);

    /** Denies {@code jti} and/or {@code sid} (either may be null) until {@code until}. */
    void deny(String jti, String sid, Instant until);
}
