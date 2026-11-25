package com.medimitra.gateway;

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

/**
 * Proves the route table actually dispatches to the right backend — in particular that the more
 * specific doctor-slots route (Scheduling) is matched before the general doctor-listing route
 * (Provider Directory), since Spring Cloud Gateway matches top-to-bottom — now that every one of
 * these routes also requires a valid token, per the jwt-dpop-filter feature.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "10000")
class RoutingIntegrationTest {

    private static final String ISSUER = "https://auth.in.medimitra.dev";
    private static final String AUDIENCE = "medimitra-api-gateway";

    private static DisposableServer patientProfileStub;
    private static DisposableServer schedulingStub;
    private static DisposableServer providerDirectoryStub;
    private static DisposableServer jwksStub;
    private static ECKey signingKey;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry registry) throws Exception {
        patientProfileStub = stubServer("patient-profile");
        schedulingStub = stubServer("scheduling");
        providerDirectoryStub = stubServer("provider-directory");
        registry.add("medimitra.gateway.backends.patient-profile", () -> "http://localhost:" + patientProfileStub.port());
        registry.add("medimitra.gateway.backends.scheduling", () -> "http://localhost:" + schedulingStub.port());
        registry.add("medimitra.gateway.backends.provider-directory", () -> "http://localhost:" + providerDirectoryStub.port());

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
        schedulingStub.disposeNow();
        providerDirectoryStub.disposeNow();
        jwksStub.disposeNow();
    }

    private static DisposableServer stubServer(String label) {
        return HttpServer.create().port(0).handle((request, response) -> response.sendString(Mono.just(label + ":" + request.uri()))).bindNow();
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
    void routesPatientRequestsToPatientProfile() throws Exception {
        webTestClient
                .get()
                .uri("/api/v1/patients/123")
                .header("Authorization", "Bearer " + validToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("patient-profile:/api/v1/patients/123");
    }

    @Test
    void routesDoctorSlotsToSchedulingNotProviderDirectory() throws Exception {
        webTestClient
                .get()
                .uri("/api/v1/doctors/42/slots")
                .header("Authorization", "Bearer " + validToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("scheduling:/api/v1/doctors/42/slots");
    }

    @Test
    void routesGeneralDoctorListingToProviderDirectory() throws Exception {
        webTestClient
                .get()
                .uri("/api/v1/doctors?specialty=cardiology")
                .header("Authorization", "Bearer " + validToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("provider-directory:/api/v1/doctors?specialty=cardiology");
    }

    @Test
    void rejectsProtectedRouteWithoutAToken() {
        webTestClient.get().uri("/api/v1/patients/123").exchange().expectStatus().isUnauthorized();
    }
}
