package com.medimitra.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.common.core.exception.TokenTamperedException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenValidatorTest {

    private static final String ISSUER = "https://auth.in.medimitra.dev";
    private static final String AUDIENCE = "medimitra-scheduling";

    private ECKey generateKey() throws Exception {
        return new ECKeyGenerator(Curve.P_256).keyID("test-kid").generate();
    }

    private String sign(ECKey key, JWTClaimsSet claims) throws Exception {
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).type(new JOSEObjectType("at+jwt")).keyID(key.getKeyID()).build(), claims);
        signedJWT.sign(new ECDSASigner(key));
        return signedJWT.serialize();
    }

    private JwtTokenValidator validatorFor(ECKey key, Instant now) {
        JwkKeySource source = (keyId, nowArg) -> keyId.equals(key.getKeyID()) ? Optional.of(key.toPublicJWK()) : Optional.empty();
        return new JwtTokenValidator(
                source, new NoOpTokenDenylist(), new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)),
                ISSUER, AUDIENCE, Duration.ofSeconds(30));
    }

    private JWTClaimsSet.Builder validClaims(Instant now) {
        return new JWTClaimsSet.Builder()
                .subject(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .jwtID(UUID.randomUUID().toString())
                .claim("region", "IN")
                .claim("roles", List.of("PATIENT"))
                .claim("scope", "read write");
    }

    @Test
    void acceptsAValidToken() throws Exception {
        ECKey key = generateKey();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String token = sign(key, validClaims(now).build());

        AuthenticatedPrincipal principal = validatorFor(key, now).validate(token);

        assertEquals(Region.IN, principal.region());
        assertTrue(principal.roles().contains("PATIENT"));
        assertTrue(principal.scopes().contains("read"));
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        ECKey key = generateKey();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String token = sign(key, validClaims(now.minusSeconds(600))
                .expirationTime(Date.from(now.minusSeconds(100)))
                .build());

        assertThrows(AuthenticationFailedException.class, () -> validatorFor(key, now).validate(token));
    }

    @Test
    void rejectsUnknownKeyId() throws Exception {
        ECKey signingKey = generateKey();
        ECKey otherKey = generateKey();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String token = sign(signingKey, validClaims(now).build());

        assertThrows(TokenTamperedException.class, () -> validatorFor(otherKey, now).validate(token));
    }

    @Test
    void rejectsWrongAudience() throws Exception {
        ECKey key = generateKey();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String token = sign(key, validClaims(now).audience("medimitra-payment").build());

        assertThrows(TokenTamperedException.class, () -> validatorFor(key, now).validate(token));
    }
}
