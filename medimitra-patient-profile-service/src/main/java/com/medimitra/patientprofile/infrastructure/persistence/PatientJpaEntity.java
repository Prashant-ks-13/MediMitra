package com.medimitra.patientprofile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Package-private: nothing outside this package should ever touch a JPA entity directly — {@link JpaPatientRepository} is the one adapter that maps to/from the domain. */
@Entity
@Table(name = "patient")
class PatientJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String region;

    private String fullName;

    private LocalDate dateOfBirth;

    private String gender;

    private String abhaId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PatientJpaEntity() {
        // JPA
    }

    PatientJpaEntity(
            UUID id,
            UUID userId,
            String region,
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            String abhaId,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.region = region;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.abhaId = abhaId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() {
        return id;
    }

    UUID getUserId() {
        return userId;
    }

    String getRegion() {
        return region;
    }

    String getFullName() {
        return fullName;
    }

    LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    String getGender() {
        return gender;
    }

    String getAbhaId() {
        return abhaId;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
