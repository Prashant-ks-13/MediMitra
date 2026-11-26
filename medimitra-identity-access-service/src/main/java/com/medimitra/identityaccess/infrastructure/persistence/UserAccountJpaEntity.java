package com.medimitra.identityaccess.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Package-private: nothing outside this package should ever touch a JPA entity directly —
 * {@link JpaUserAccountRepository} is the one adapter that maps to/from the domain.
 */
@Entity
@Table(name = "user_account")
class UserAccountJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private Instant createdAt;

    protected UserAccountJpaEntity() {
        // JPA
    }

    UserAccountJpaEntity(UUID id, String email, String role, String region, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.region = region;
        this.createdAt = createdAt;
    }

    UUID getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    String getRole() {
        return role;
    }

    String getRegion() {
        return region;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
