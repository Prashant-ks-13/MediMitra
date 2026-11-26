package com.medimitra.identityaccess.infrastructure.client;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.error.ErrorCode;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.common.core.exception.ExternalServiceUnavailableException;
import com.medimitra.common.security.ServiceTokenProvider;
import com.medimitra.identityaccess.IdentityErrorCodes;
import com.medimitra.identityaccess.application.port.KeycloakClient;
import com.medimitra.identityaccess.application.port.TokenResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Calls Keycloak's admin and token REST APIs directly. */
public class RestKeycloakClient implements KeycloakClient {

    private record KeycloakCredential(String type, String value, boolean temporary) {
    }

    private record KeycloakUserRepresentation(
            String username, String email, boolean enabled, List<KeycloakCredential> credentials, Map<String, List<String>> attributes) {
    }

    private record KeycloakRoleRepresentation(String id, String name) {
    }

    private record KeycloakTokenResponse(String access_token, String refresh_token, long expires_in, String token_type) {
    }

    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;
    private final String realm;
    private final String adminAudience;
    private final String publicClientId;
    private final String publicClientSecret;

    public RestKeycloakClient(
            RestClient.Builder restClientBuilder,
            String baseUrl,
            ServiceTokenProvider serviceTokenProvider,
            String realm,
            String adminAudience,
            String publicClientId,
            String publicClientSecret) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.serviceTokenProvider = serviceTokenProvider;
        this.realm = realm;
        this.adminAudience = adminAudience;
        this.publicClientId = publicClientId;
        this.publicClientSecret = publicClientSecret;
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

    @Override
    public TokenResponse passwordLogin(String email, String rawPassword, String dpopProof) {
        String body = "grant_type=password"
                + "&client_id=" + encode(publicClientId)
                + "&client_secret=" + encode(publicClientSecret)
                + "&username=" + encode(email)
                + "&password=" + encode(rawPassword);
        return requestToken(body, dpopProof, IdentityErrorCodes.LOGIN_FAILED, "login failed for " + email);
    }

    @Override
    public TokenResponse tokenExchangeFor(UUID userId, String dpopProof) {
        String adminToken = serviceTokenProvider.acquireToken(adminAudience);
        String body = "grant_type=" + encode("urn:ietf:params:oauth:grant-type:token-exchange")
                + "&client_id=" + encode(publicClientId)
                + "&client_secret=" + encode(publicClientSecret)
                + "&subject_token=" + encode(adminToken)
                + "&subject_token_type=" + encode("urn:ietf:params:oauth:token-type:access_token")
                + "&requested_subject=" + encode(userId.toString());
        return requestToken(body, dpopProof, IdentityErrorCodes.OTP_INVALID_OR_EXPIRED, "token exchange failed for " + userId);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken, String dpopProof) {
        String body = "grant_type=refresh_token"
                + "&client_id=" + encode(publicClientId)
                + "&client_secret=" + encode(publicClientSecret)
                + "&refresh_token=" + encode(refreshToken);
        return requestToken(body, dpopProof, IdentityErrorCodes.REFRESH_FAILED, "refresh failed");
    }

    @Override
    public void logout(String refreshToken) {
        String body = "client_id=" + encode(publicClientId) + "&client_secret=" + encode(publicClientSecret) + "&refresh_token="
                + encode(refreshToken);
        try {
            restClient
                    .post()
                    .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ExternalServiceUnavailableException(IdentityErrorCodes.LOGOUT_FAILED, "logout failed at Keycloak", e);
        }
    }

    private TokenResponse requestToken(String formBody, String dpopProof, ErrorCode failureCode, String failureMessage) {
        try {
            RestClient.RequestBodySpec request = restClient
                    .post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED);
            if (dpopProof != null) {
                request = request.header("DPoP", dpopProof);
            }
            KeycloakTokenResponse response = request.body(formBody).retrieve().body(KeycloakTokenResponse.class);
            return new TokenResponse(response.access_token(), response.refresh_token(), response.expires_in(), response.token_type());
        } catch (RestClientResponseException e) {
            throw new AuthenticationFailedException(failureCode, failureMessage);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
