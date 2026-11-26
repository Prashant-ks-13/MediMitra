package com.medimitra.patientprofile.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Package-private: only {@link JpaPatientRepository} should talk to Spring Data directly. */
interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, UUID> {
    boolean existsByUserId(UUID userId);
}
