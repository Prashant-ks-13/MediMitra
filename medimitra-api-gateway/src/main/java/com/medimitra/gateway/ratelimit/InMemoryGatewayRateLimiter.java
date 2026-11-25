package com.medimitra.gateway.ratelimit;

import com.medimitra.common.core.ClockProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window counter per key. Single-instance default; a multi-instance gateway deployment
 * needs a shared (Redis) implementation instead, since counters here are not shared across replicas.
 */
public final class InMemoryGatewayRateLimiter implements GatewayRateLimiter {

    private record Window(Instant windowStart, AtomicInteger count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ClockProvider clockProvider;
    private final int limitPerWindow;
    private final Duration windowDuration;

    public InMemoryGatewayRateLimiter(ClockProvider clockProvider, int limitPerWindow, Duration windowDuration) {
        this.clockProvider = clockProvider;
        this.limitPerWindow = limitPerWindow;
        this.windowDuration = windowDuration;
    }

    @Override
    public RateLimitDecision tryConsume(String key) {
        Instant now = clockProvider.now();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.windowStart().plus(windowDuration))) {
                return new Window(now, new AtomicInteger(0));
            }
            return existing;
        });
        int count = window.count().incrementAndGet();
        boolean allowed = count <= limitPerWindow;
        long remaining = Math.max(0, limitPerWindow - count);
        long retryAfterSeconds = Math.max(0, Duration.between(now, window.windowStart().plus(windowDuration)).getSeconds());
        return new RateLimitDecision(allowed, remaining, allowed ? 0 : retryAfterSeconds);
    }
}
