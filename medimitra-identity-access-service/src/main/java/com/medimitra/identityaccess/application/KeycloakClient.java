package com.medimitra.identityaccess.application;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import java.util.UUID;

/** Port to Keycloak's admin API; {@code infrastructure.client} provides the REST adapter. */
public interface KeycloakClient {

    /** Creates the realm user and assigns {@code role}; returns Keycloak's generated user id. */
    UUID createUser(String email, String rawPassword, UserRole role, Region region);
}
