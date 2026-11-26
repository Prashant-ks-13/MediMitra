package com.medimitra.common.security;

import java.time.Instant;

/** Nothing is ever treated as revoked and {@link #deny} is silently discarded; for contexts that only ever read (e.g. the gateway). */
public final class NoOpTokenDenylist implements TokenDenylist {

    @Override
    public boolean isDenied(String jti, String sid) {
        return false;
    }

    @Override
    public void deny(String jti, String sid, Instant until) {
        // intentionally discarded
    }
}
