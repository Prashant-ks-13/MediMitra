package com.medimitra.identityaccess.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.identityaccess.application.port.TokenResponse;
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

    private RestKeycloakClient clientFor(int port) {
        return new RestKeycloakClient(
                RestClient.builder(), "http://localhost:" + port, audience -> "fake-admin-token", "medimitra-in", "realm-management",
                "medimitra-patient-app", "changeme");
    }

    private static void sendTokenResponse(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] body = "{\"access_token\":\"access-1\",\"refresh_token\":\"refresh-1\",\"expires_in\":300,\"token_type\":\"DPoP\"}".getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

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

        RestKeycloakClient client = clientFor(server.getAddress().getPort());

        UUID result = client.createUser("a@b.com", "password123", UserRole.PATIENT, Region.IN);

        assertNotNull(result);
        assertEquals(createdUserId, result);
        org.junit.jupiter.api.Assertions.assertTrue(roleMappingBody.get().contains("PATIENT"));
    }

    @Test
    void passwordLoginReturnsTokensAndForwardsDpopHeader() throws IOException {
        AtomicReference<String> capturedDpopHeader = new AtomicReference<>();
        AtomicReference<String> capturedBody = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/realms/medimitra-in/protocol/openid-connect/token", exchange -> {
            capturedDpopHeader.set(exchange.getRequestHeaders().getFirst("DPoP"));
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes()));
            sendTokenResponse(exchange);
        });
        server.start();

        RestKeycloakClient client = clientFor(server.getAddress().getPort());

        TokenResponse response = client.passwordLogin("a@b.com", "password123", "dpop-proof-1");

        assertEquals("access-1", response.accessToken());
        assertEquals("refresh-1", response.refreshToken());
        assertEquals(300, response.expiresInSeconds());
        assertEquals("dpop-proof-1", capturedDpopHeader.get());
        org.junit.jupiter.api.Assertions.assertTrue(capturedBody.get().contains("grant_type=password"));
    }

    @Test
    void tokenExchangeReturnsTokensForTheRequestedSubject() throws IOException {
        UUID userId = UUID.randomUUID();
        AtomicReference<String> capturedBody = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/realms/medimitra-in/protocol/openid-connect/token", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes()));
            sendTokenResponse(exchange);
        });
        server.start();

        RestKeycloakClient client = clientFor(server.getAddress().getPort());

        TokenResponse response = client.tokenExchangeFor(userId, null);

        assertEquals("access-1", response.accessToken());
        org.junit.jupiter.api.Assertions.assertTrue(capturedBody.get().contains("requested_subject=" + userId));
        org.junit.jupiter.api.Assertions.assertTrue(capturedBody.get().contains("token-exchange"));
    }

    @Test
    void refreshTokenReturnsNewTokens() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/realms/medimitra-in/protocol/openid-connect/token", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes()));
            sendTokenResponse(exchange);
        });
        server.start();

        RestKeycloakClient client = clientFor(server.getAddress().getPort());

        TokenResponse response = client.refreshToken("old-refresh-token", null);

        assertEquals("access-1", response.accessToken());
        org.junit.jupiter.api.Assertions.assertTrue(capturedBody.get().contains("grant_type=refresh_token"));
        org.junit.jupiter.api.Assertions.assertTrue(capturedBody.get().contains("refresh_token=old-refresh-token"));
    }

    @Test
    void logoutCallsTheEndSessionEndpoint() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/realms/medimitra-in/protocol/openid-connect/logout", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes()));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();

        RestKeycloakClient client = clientFor(server.getAddress().getPort());

        client.logout("refresh-1");

        org.junit.jupiter.api.Assertions.assertTrue(capturedBody.get().contains("refresh_token=refresh-1"));
    }
}
