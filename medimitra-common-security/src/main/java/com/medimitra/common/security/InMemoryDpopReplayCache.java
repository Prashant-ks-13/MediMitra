package com.medimitra.common.security;

import com.medimitra.common.core.ClockProvider;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Single-instance default; a multi-instance deployment needs a shared (Redis) implementation instead. */
public final class InMemoryDpopReplayCache implements DpopReplayCache {

    private final Map<String, Instant> seen = new ConcurrentHashMap<>();
    private final ClockProvider clockProvider;

    public InMemoryDpopReplayCache(ClockProvider clockProvider) {
        this.clockProvider = clockProvider;
    }

    @Override
    public boolean isReplayed(String jti) {
        sweep();
        return seen.containsKey(jti);
    }

    @Override
    public void remember(String jti, Instant expiresAt) {
        seen.put(jti, expiresAt);
    }

    private void sweep() {
        Instant now = clockProvider.now();
        seen.values().removeIf(expiresAt -> expiresAt.isBefore(now));
    }
}
