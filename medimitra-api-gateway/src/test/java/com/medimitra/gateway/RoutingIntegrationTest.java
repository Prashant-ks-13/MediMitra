package com.medimitra.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
 * (Provider Directory), since Spring Cloud Gateway matches top-to-bottom.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "10000")
class RoutingIntegrationTest {

    private static DisposableServer patientProfileStub;
    private static DisposableServer schedulingStub;
    private static DisposableServer providerDirectoryStub;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void backendProperties(DynamicPropertyRegistry registry) {
        patientProfileStub = stubServer("patient-profile");
        schedulingStub = stubServer("scheduling");
        providerDirectoryStub = stubServer("provider-directory");
        registry.add("medimitra.gateway.backends.patient-profile", () -> "http://localhost:" + patientProfileStub.port());
        registry.add("medimitra.gateway.backends.scheduling", () -> "http://localhost:" + schedulingStub.port());
        registry.add("medimitra.gateway.backends.provider-directory", () -> "http://localhost:" + providerDirectoryStub.port());
    }

    @AfterAll
    static void stopStubs() {
        patientProfileStub.disposeNow();
        schedulingStub.disposeNow();
        providerDirectoryStub.disposeNow();
    }

    private static DisposableServer stubServer(String label) {
        return HttpServer.create().port(0).handle((request, response) -> response.sendString(Mono.just(label + ":" + request.uri()))).bindNow();
    }

    @Test
    void routesPatientRequestsToPatientProfile() {
        webTestClient
                .get()
                .uri("/api/v1/patients/123")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("patient-profile:/api/v1/patients/123");
    }

    @Test
    void routesDoctorSlotsToSchedulingNotProviderDirectory() {
        webTestClient
                .get()
                .uri("/api/v1/doctors/42/slots")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("scheduling:/api/v1/doctors/42/slots");
    }

    @Test
    void routesGeneralDoctorListingToProviderDirectory() {
        webTestClient
                .get()
                .uri("/api/v1/doctors?specialty=cardiology")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("provider-directory:/api/v1/doctors?specialty=cardiology");
    }
}
