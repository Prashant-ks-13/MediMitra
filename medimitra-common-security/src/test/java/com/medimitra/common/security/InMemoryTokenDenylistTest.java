package com.medimitra.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.SystemClockProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class InMemoryTokenDenylistTest {

    @Test
    void deniesByEitherJtiOrSid() {
        InMemoryTokenDenylist denylist = new InMemoryTokenDenylist(new SystemClockProvider());
        Instant until = Instant.now().plusSeconds(60);

        denylist.deny("jti-1", null, until);
        denylist.deny(null, "sid-1", until);

        assertTrue(denylist.isDenied("jti-1", "anything"));
        assertTrue(denylist.isDenied("anything", "sid-1"));
        assertFalse(denylist.isDenied("jti-2", "sid-2"));
    }

    @Test
    void stopsDenyingOnceExpired() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AtomicReference<Instant> clock = new AtomicReference<>(now);
        var clockProvider = new com.medimitra.common.core.ClockProvider() {
            @Override
            public Instant now() {
                return clock.get();
            }

            @Override
            public Clock clock() {
                return Clock.fixed(clock.get(), ZoneOffset.UTC);
            }
        };
        InMemoryTokenDenylist denylist = new InMemoryTokenDenylist(clockProvider);
        denylist.deny("jti-1", null, now.plusSeconds(60));

        assertTrue(denylist.isDenied("jti-1", null));

        clock.set(now.plusSeconds(61));

        assertFalse(denylist.isDenied("jti-1", null));
    }
}
