package com.medimitra.gateway.ratelimit;

/** Backs per-key rate limiting at the edge; production should wire a Redis-backed implementation for multi-instance correctness. */
public interface GatewayRateLimiter {
    RateLimitDecision tryConsume(String key);
}
