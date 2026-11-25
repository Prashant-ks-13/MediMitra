package com.medimitra.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.security.DpopProofVerifier;
import com.medimitra.common.security.InMemoryDpopReplayCache;
import com.medimitra.common.security.JwkKeySource;
import com.medimitra.common.security.JwtTokenValidator;
import com.medimitra.common.security.NoOpTokenDenylist;
import com.medimitra.common.security.RegionGuard;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class JwtDpopGlobalFilterTest {

    private static final String ISSUER = "https://auth.in.medimitra.dev";
    private static final String AUDIENCE = "medimitra-api-gateway";

    private ECKey generateKey() throws Exception {
        return new ECKeyGenerator(Curve.P_256).keyID("test-kid").generate();
    }

    private String sign(ECKey key, JWTClaimsSet claims) throws Exception {
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).type(new JOSEObjectType("at+jwt")).keyID(key.getKeyID()).build(), claims);
        signedJWT.sign(new ECDSASigner(key));
        return signedJWT.serialize();
    }

    private JwtDpopGlobalFilter filterFor(ECKey key, Instant now) {
        JwkKeySource source = (keyId, nowArg) -> keyId.equals(key.getKeyID()) ? Optional.of(key.toPublicJWK()) : Optional.empty();
        SystemClockProvider clockProvider = new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC));
        JwtTokenValidator validator = new JwtTokenValidator(source, new NoOpTokenDenylist(), clockProvider, ISSUER, AUDIENCE, Duration.ofSeconds(30));
        DpopProofVerifier dpopProofVerifier = new DpopProofVerifier(new InMemoryDpopReplayCache(clockProvider), clockProvider, Duration.ofSeconds(60));
        RegionGuard regionGuard = new RegionGuard(Region.IN);
        return new JwtDpopGlobalFilter(validator, dpopProofVerifier, regionGuard, clockProvider, new ObjectMapper());
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
                .claim("scope", "read");
    }

    private ServerWebExchange exchangeFor(String path, boolean publicRoute, String authorizationHeader) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(path);
        if (authorizationHeader != null) {
            builder = builder.header("Authorization", authorizationHeader);
        }
        MockServerWebExchange exchange = MockServerWebExchange.from(builder.build());
        Route route = Route.async()
                .id("test-route")
                .uri(URI.create("http://localhost"))
                .predicate(ex -> true)
                .metadata(JwtDpopGlobalFilter.PUBLIC_ROUTE_METADATA_KEY, publicRoute)
                .build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
        return exchange;
    }

    private GatewayFilterChain chainRecording(AtomicBoolean called) {
        return ex -> {
            called.set(true);
            return Mono.empty();
        };
    }

    @Test
    void publicRouteBypassesValidation() throws Exception {
        JwtDpopGlobalFilter filter = filterFor(generateKey(), Instant.parse("2026-01-01T00:00:00Z"));
        ServerWebExchange exchange = exchangeFor("/api/v1/auth/login", true, null);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainRecording(chainCalled)).block();

        assertTrue(chainCalled.get());
    }

    @Test
    void protectedRouteWithoutTokenIsRejected() throws Exception {
        JwtDpopGlobalFilter filter = filterFor(generateKey(), Instant.parse("2026-01-01T00:00:00Z"));
        ServerWebExchange exchange = exchangeFor("/api/v1/patients/1", false, null);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainRecording(chainCalled)).block();

        assertFalse(chainCalled.get());
        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void protectedRouteWithValidTokenProceeds() throws Exception {
        ECKey key = generateKey();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        JwtDpopGlobalFilter filter = filterFor(key, now);
        String token = sign(key, validClaims(now).build());
        ServerWebExchange exchange = exchangeFor("/api/v1/patients/1", false, "Bearer " + token);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainRecording(chainCalled)).block();

        assertTrue(chainCalled.get());
    }

    @Test
    void protectedRouteWithWrongRegionIsRejected() throws Exception {
        ECKey key = generateKey();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        JwtDpopGlobalFilter filter = filterFor(key, now);
        String token = sign(key, validClaims(now).claim("region", "EU").build());
        ServerWebExchange exchange = exchangeFor("/api/v1/patients/1", false, "Bearer " + token);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainRecording(chainCalled)).block();

        assertFalse(chainCalled.get());
        assertEquals(403, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void protectedRouteWithExpiredTokenIsRejected() throws Exception {
        ECKey key = generateKey();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        JwtDpopGlobalFilter filter = filterFor(key, now);
        String token = sign(key, validClaims(now.minusSeconds(600)).expirationTime(Date.from(now.minusSeconds(100))).build());
        ServerWebExchange exchange = exchangeFor("/api/v1/patients/1", false, "Bearer " + token);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainRecording(chainCalled)).block();

        assertFalse(chainCalled.get());
        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }
}
