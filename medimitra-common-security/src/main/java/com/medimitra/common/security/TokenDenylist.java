package com.medimitra.common.security;

/**
 * Backs the {@code jti}/{@code sid} revocation check in section 8.3, rule 7. Production should
 * wire a Redis-backed bean; this port lets a service do that without touching {@link JwtTokenValidator}.
 */
public interface TokenDenylist {
    boolean isDenied(String jti, String sid);
}
