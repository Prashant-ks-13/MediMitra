package com.medimitra.identityaccess.application.port;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import java.util.UUID;

/** Port to Keycloak's admin and token APIs; {@code infrastructure.client} provides the REST adapter. */
public interface KeycloakClient {

    /** Creates the realm user and assigns {@code role}; returns Keycloak's generated user id. */
    UUID createUser(String email, String rawPassword, UserRole role, Region region);

    /**
     * Direct Access Grant (password) login. Keycloak's own realm/authentication-flow configuration
     * decides whether MFA is demanded for this account (e.g. staff roles) — this call simply proxies
     * whatever Keycloak does, success or failure; {@code dpopProof}, if present, binds the issued
     * token's {@code cnf.jkt} to the caller's key (section 8.1).
     */
    TokenResponse passwordLogin(String email, String rawPassword, String dpopProof);

    /**
     * RFC 8693 token exchange: mints a token for {@code userId} using this service's own admin
     * credentials as the subject token, once OTP verification (owned entirely by this service, not
     * Keycloak) has already confirmed the caller's identity.
     */
    TokenResponse tokenExchangeFor(UUID userId, String dpopProof);

    /**
     * Refresh-token grant. Rotation and reuse detection (section 8.4: "presenting an old one
     * revokes the whole token family") are Keycloak realm settings (Revoke Refresh Token enabled),
     * not application logic — this call simply proxies whatever Keycloak decides.
     */
    TokenResponse refreshToken(String refreshToken, String dpopProof);

    /** Ends the session in Keycloak for {@code refreshToken} (RP-initiated logout). */
    void logout(String refreshToken);
}
