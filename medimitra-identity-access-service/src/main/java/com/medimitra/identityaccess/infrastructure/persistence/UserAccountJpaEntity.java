package com.medimitra.identityaccess.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class UserAccountJpaEntity {

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

    public UserAccountJpaEntity(UUID id, String email, String role, String region, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.region = region;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getRegion() {
        return region;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
