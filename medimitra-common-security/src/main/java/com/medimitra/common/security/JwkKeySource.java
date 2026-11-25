package com.medimitra.common.security;

import com.nimbusds.jose.jwk.JWK;
import java.time.Instant;
import java.util.Optional;

/** Port {@link JwtTokenValidator} depends on, so it can be unit-tested without a real JWKS endpoint. */
public interface JwkKeySource {
    Optional<JWK> findByKeyId(String keyId, Instant now);
}
