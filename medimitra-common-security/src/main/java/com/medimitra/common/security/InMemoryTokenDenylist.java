package com.medimitra.common.security;

import com.medimitra.common.core.ClockProvider;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Single-instance default; a multi-instance deployment needs a shared (Redis) implementation instead. */
public final class InMemoryTokenDenylist implements TokenDenylist {

    private final Map<String, Instant> deniedJtis = new ConcurrentHashMap<>();
    private final Map<String, Instant> deniedSids = new ConcurrentHashMap<>();
    private final ClockProvider clockProvider;

    public InMemoryTokenDenylist(ClockProvider clockProvider) {
        this.clockProvider = clockProvider;
    }

    @Override
    public boolean isDenied(String jti, String sid) {
        sweep();
        return (jti != null && deniedJtis.containsKey(jti)) || (sid != null && deniedSids.containsKey(sid));
    }

    @Override
    public void deny(String jti, String sid, Instant until) {
        if (jti != null) {
            deniedJtis.put(jti, until);
        }
        if (sid != null) {
            deniedSids.put(sid, until);
        }
    }

    private void sweep() {
        Instant now = clockProvider.now();
        deniedJtis.values().removeIf(expiresAt -> expiresAt.isBefore(now));
        deniedSids.values().removeIf(expiresAt -> expiresAt.isBefore(now));
    }
}
