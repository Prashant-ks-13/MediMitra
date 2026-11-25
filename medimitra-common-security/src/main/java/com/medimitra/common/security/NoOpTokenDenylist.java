package com.medimitra.common.security;

/** Default when no Redis-backed denylist is wired: nothing is ever treated as revoked. */
public final class NoOpTokenDenylist implements TokenDenylist {

    @Override
    public boolean isDenied(String jti, String sid) {
        return false;
    }
}
