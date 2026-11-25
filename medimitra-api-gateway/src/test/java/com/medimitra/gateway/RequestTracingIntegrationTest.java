package com.medimitra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.medimitra.gateway.tracing.CorrelationIdGlobalFilter;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/** Proves the gateway mints/reuses and forwards {@code X-MediMitra-Correlation-Id} end to end. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "10000")
class RequestTracingIntegrationTest {

    private static final String ISSUER = "https://auth.in.medimitra.dev";
    private static final String AUDIENCE = "medimitra-api-gateway";
    private static final AtomicReference<String> forwardedCorrelationId = new AtomicReference<>();

    private static DisposableServer patientProfileStub;
    private static DisposableServer jwksStub;
    private static ECKey signingKey;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry registry) throws Exception {
        patientProfileStub = HttpServer.create()
                .port(0)
                .handle((request, response) -> {
                    forwardedCorrelationId.set(request.requestHeaders().get(CorrelationIdGlobalFilter.HEADER));
                    return response.sendString(Mono.just("ok"));
                })
                .bindNow();
        registry.add("medimitra.gateway.backends.patient-profile", () -> "http://localhost:" + patientProfileStub.port());

        signingKey = new ECKeyGenerator(Curve.P_256).keyID("test-kid").generate();
        jwksStub = HttpServer.create()
                .port(0)
                .handle((request, response) -> response.sendString(Mono.just(new JWKSet(signingKey.toPublicJWK()).toString())))
                .bindNow();
        registry.add("medimitra.security.jwks-uri", () -> "http://localhost:" + jwksStub.port());
        registry.add("medimitra.security.issuer", () -> ISSUER);
        registry.add("medimitra.security.audience", () -> AUDIENCE);
        registry.add("medimitra.security.region", () -> "IN");
    }

    @AfterAll
    static void stopStubs() {
        patientProfileStub.disposeNow();
        jwksStub.disposeNow();
    }

    private static String validToken() throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .jwtID(UUID.randomUUID().toString())
                .claim("region", "IN")
                .claim("roles", List.of("PATIENT"))
                .claim("scope", "read")
                .build();
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).type(new JOSEObjectType("at+jwt")).keyID(signingKey.getKeyID()).build(), claims);
        signedJWT.sign(new ECDSASigner(signingKey));
        return signedJWT.serialize();
    }

    @Test
    void mintsAndForwardsACorrelationIdWhenNoneSupplied() throws Exception {
        webTestClient
                .get()
                .uri("/api/v1/patients/123")
                .header("Authorization", "Bearer " + validToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .exists(CorrelationIdGlobalFilter.HEADER);

        assertNotNull(forwardedCorrelationId.get());
    }

    @Test
    void reusesAndForwardsAnInboundCorrelationId() throws Exception {
        webTestClient
                .get()
                .uri("/api/v1/patients/123")
                .header("Authorization", "Bearer " + validToken())
                .header(CorrelationIdGlobalFilter.HEADER, "corr-abc-123")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(CorrelationIdGlobalFilter.HEADER, "corr-abc-123");

        assertEquals("corr-abc-123", forwardedCorrelationId.get());
    }

    @Test
    void echoesACorrelationIdEvenOnRejection() {
        webTestClient
                .get()
                .uri("/api/v1/patients/123")
                .header(CorrelationIdGlobalFilter.HEADER, "corr-rejected")
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectHeader()
                .valueEquals(CorrelationIdGlobalFilter.HEADER, "corr-rejected");
    }
}
