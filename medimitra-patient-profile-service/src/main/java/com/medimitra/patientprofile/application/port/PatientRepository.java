package com.medimitra.patientprofile.application.port;

import com.medimitra.patientprofile.domain.Patient;
import java.util.Optional;
import java.util.UUID;

/** Outbound port the application layer depends on; {@code infrastructure.persistence} provides the JPA adapter. */
public interface PatientRepository {
    boolean existsByUserId(UUID userId);

    Optional<Patient> findById(UUID id);

    void save(Patient patient);
}
