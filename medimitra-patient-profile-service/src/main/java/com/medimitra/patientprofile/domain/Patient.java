package com.medimitra.patientprofile.domain;

import com.medimitra.common.core.Region;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * {@code id} is this service's own aggregate id, distinct from {@code userId} (README section 9.1
 * ERD: {@code PATIENT.id} vs {@code PATIENT.user_id}) — cross-service references are ids only.
 * Pure domain object — no Spring, JPA or Kafka imports, enforced by {@code LayeredArchitectureTest}.
 */
public record Patient(
        UUID id,
        UUID userId,
        Region region,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String abhaId,
        Instant createdAt,
        Instant updatedAt) {

    public Patient {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /** A bare record created the moment {@code user.registered} arrives; demographics are filled in later. */
    public static Patient newlyRegistered(UUID id, UUID userId, Region region, Instant now) {
        return new Patient(id, userId, region, null, null, null, null, now, now);
    }

    public Patient withDemographics(String fullName, LocalDate dateOfBirth, String gender, String abhaId, Instant now) {
        return new Patient(id, userId, region, fullName, dateOfBirth, gender, abhaId, createdAt, now);
    }

    public boolean isOwnedBy(UUID requestingUserId) {
        return userId.equals(requestingUserId);
    }
}
