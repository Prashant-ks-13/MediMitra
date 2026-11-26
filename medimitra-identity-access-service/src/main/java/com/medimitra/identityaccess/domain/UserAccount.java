package com.medimitra.identityaccess.domain;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The local record of a registered account; the same {@code id} also identifies this user in
 * Keycloak. Pure domain object — no Spring, JPA or Kafka imports (README section 3.1), enforced
 * by {@code LayeredArchitectureTest}.
 */
public record UserAccount(UUID id, String email, UserRole role, Region region, Instant createdAt) {

    public UserAccount {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
