package com.medimitra.gateway.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.SystemClockProvider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class InMemoryGatewayRateLimiterTest {

    @Test
    void allowsUpToTheLimitThenDenies() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        InMemoryGatewayRateLimiter limiter =
                new InMemoryGatewayRateLimiter(new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)), 2, Duration.ofSeconds(60));

        assertTrue(limiter.tryConsume("ip:1.2.3.4").allowed());
        assertTrue(limiter.tryConsume("ip:1.2.3.4").allowed());
        assertFalse(limiter.tryConsume("ip:1.2.3.4").allowed());
    }

    @Test
    void tracksEachKeyIndependently() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        InMemoryGatewayRateLimiter limiter =
                new InMemoryGatewayRateLimiter(new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)), 1, Duration.ofSeconds(60));

        assertTrue(limiter.tryConsume("ip:1.2.3.4").allowed());
        assertTrue(limiter.tryConsume("ip:5.6.7.8").allowed());
        assertFalse(limiter.tryConsume("ip:1.2.3.4").allowed());
    }

    @Test
    void resetsOnceTheWindowElapses() {
        Instant windowStart = Instant.parse("2026-01-01T00:00:00Z");
        java.util.concurrent.atomic.AtomicReference<Instant> clock = new java.util.concurrent.atomic.AtomicReference<>(windowStart);
        com.medimitra.common.core.ClockProvider clockProvider = new com.medimitra.common.core.ClockProvider() {
            @Override
            public Instant now() {
                return clock.get();
            }

            @Override
            public Clock clock() {
                return Clock.fixed(clock.get(), ZoneOffset.UTC);
            }
        };
        InMemoryGatewayRateLimiter limiter = new InMemoryGatewayRateLimiter(clockProvider, 1, Duration.ofSeconds(60));

        assertTrue(limiter.tryConsume("ip:1.2.3.4").allowed());
        assertFalse(limiter.tryConsume("ip:1.2.3.4").allowed());

        clock.set(windowStart.plusSeconds(61));

        assertTrue(limiter.tryConsume("ip:1.2.3.4").allowed());
    }
}
