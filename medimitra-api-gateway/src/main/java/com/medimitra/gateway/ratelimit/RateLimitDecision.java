package com.medimitra.gateway.ratelimit;

/** Whether this call may proceed, plus enough detail to set standard rate-limit response headers. */
public record RateLimitDecision(boolean allowed, long remaining, long retryAfterSeconds) {
}
