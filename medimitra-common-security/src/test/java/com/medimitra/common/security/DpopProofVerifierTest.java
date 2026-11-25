package com.medimitra.common.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.medimitra.common.core.SystemClockProvider;
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
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DpopProofVerifierTest {

    private static final String METHOD = "POST";
    private static final String URI = "https://api.medimitra.dev/api/v1/appointments";

    private String signProof(ECKey key, Instant iat, String jti) throws Exception {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(new JOSEObjectType("dpop+jwt"))
                .jwk(key.toPublicJWK())
                .build();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("htm", METHOD)
                .claim("htu", URI)
                .issueTime(Date.from(iat))
                .jwtID(jti)
                .build();
        SignedJWT proof = new SignedJWT(header, claims);
        proof.sign(new ECDSASigner(key));
        return proof.serialize();
    }

    @Test
    void acceptsAFreshMatchingProof() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String thumbprint = key.computeThumbprint().toString();
        String proof = signProof(key, now, UUID.randomUUID().toString());

        DpopProofVerifier verifier = new DpopProofVerifier(
                new InMemoryDpopReplayCache(new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC))),
                new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)),
                Duration.ofSeconds(60));

        assertDoesNotThrow(() -> verifier.verify(proof, METHOD, URI, thumbprint));
    }

    @Test
    void rejectsReplayedProof() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String thumbprint = key.computeThumbprint().toString();
        String jti = UUID.randomUUID().toString();
        String proof = signProof(key, now, jti);

        DpopProofVerifier verifier = new DpopProofVerifier(
                new InMemoryDpopReplayCache(new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC))),
                new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)),
                Duration.ofSeconds(60));

        verifier.verify(proof, METHOD, URI, thumbprint);
        assertThrows(TokenTamperedException.class, () -> verifier.verify(proof, METHOD, URI, thumbprint));
    }

    @Test
    void rejectsKeyNotMatchingTokenBinding() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String proof = signProof(key, now, UUID.randomUUID().toString());

        DpopProofVerifier verifier = new DpopProofVerifier(
                new InMemoryDpopReplayCache(new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC))),
                new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)),
                Duration.ofSeconds(60));

        assertThrows(TokenTamperedException.class, () -> verifier.verify(proof, METHOD, URI, "wrong-thumbprint"));
    }
}
