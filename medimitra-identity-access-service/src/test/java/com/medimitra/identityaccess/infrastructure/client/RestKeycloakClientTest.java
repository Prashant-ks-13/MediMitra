package com.medimitra.identityaccess.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RestKeycloakClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createsUserAndAssignsRole() throws IOException {
        UUID createdUserId = UUID.randomUUID();
        AtomicReference<String> roleMappingBody = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/admin/realms/medimitra-in/users", exchange -> {
            exchange.getResponseHeaders().add("Location", "/admin/realms/medimitra-in/users/" + createdUserId);
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        server.createContext("/admin/realms/medimitra-in/roles/PATIENT", exchange -> {
            byte[] body = "{\"id\":\"role-id-1\",\"name\":\"PATIENT\"}".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/admin/realms/medimitra-in/users/" + createdUserId + "/role-mappings/realm", exchange -> {
            roleMappingBody.set(new String(exchange.getRequestBody().readAllBytes()));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();

        RestKeycloakClient client = new RestKeycloakClient(
                RestClient.builder(), "http://localhost:" + server.getAddress().getPort(), audience -> "fake-admin-token", "medimitra-in", "realm-management");

        UUID result = client.createUser("a@b.com", "password123", UserRole.PATIENT, Region.IN);

        assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(createdUserId, result);
        org.junit.jupiter.api.Assertions.assertTrue(roleMappingBody.get().contains("PATIENT"));
    }
}
