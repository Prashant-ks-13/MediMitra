package com.medimitra.identityaccess.infrastructure.client;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.common.security.ServiceTokenProvider;
import com.medimitra.identityaccess.application.KeycloakClient;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/** Calls Keycloak's admin REST API directly to create a realm user and assign its role. */
public class RestKeycloakClient implements KeycloakClient {

    private record KeycloakCredential(String type, String value, boolean temporary) {
    }

    private record KeycloakUserRepresentation(
            String username, String email, boolean enabled, List<KeycloakCredential> credentials, Map<String, List<String>> attributes) {
    }

    private record KeycloakRoleRepresentation(String id, String name) {
    }

    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;
    private final String realm;
    private final String adminAudience;

    public RestKeycloakClient(RestClient.Builder restClientBuilder, String baseUrl, ServiceTokenProvider serviceTokenProvider, String realm, String adminAudience) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.serviceTokenProvider = serviceTokenProvider;
        this.realm = realm;
        this.adminAudience = adminAudience;
    }

    @Override
    public UUID createUser(String email, String rawPassword, UserRole role, Region region) {
        String adminToken = serviceTokenProvider.acquireToken(adminAudience);
        KeycloakUserRepresentation user = new KeycloakUserRepresentation(
                email, email, true, List.of(new KeycloakCredential("password", rawPassword, false)), Map.of("region", List.of(region.name())));
        ResponseEntity<Void> response = restClient
                .post()
                .uri("/admin/realms/{realm}/users", realm)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(user)
                .retrieve()
                .toBodilessEntity();
        UUID userId = extractUserIdFromLocation(response.getHeaders().getLocation());
        assignRealmRole(userId, role, adminToken);
        return userId;
    }

    private void assignRealmRole(UUID userId, UserRole role, String adminToken) {
        KeycloakRoleRepresentation roleRepresentation = restClient
                .get()
                .uri("/admin/realms/{realm}/roles/{roleName}", realm, role.name())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(KeycloakRoleRepresentation.class);
        restClient
                .post()
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(List.of(roleRepresentation))
                .retrieve()
                .toBodilessEntity();
    }

    private static UUID extractUserIdFromLocation(URI location) {
        String path = location.getPath();
        return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
    }
}
