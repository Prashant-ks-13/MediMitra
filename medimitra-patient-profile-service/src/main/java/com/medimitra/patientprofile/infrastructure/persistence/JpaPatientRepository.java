package com.medimitra.patientprofile.infrastructure.persistence;

import com.medimitra.common.core.Region;
import com.medimitra.patientprofile.application.port.PatientRepository;
import com.medimitra.patientprofile.domain.Patient;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: translates between the domain's {@link Patient} and the package-private JPA entity.
 * Self-registering via {@code @Repository} rather than an explicit {@code @Bean} in {@code config}
 * — a factory method in a different package couldn't reference the package-private
 * {@link PatientJpaRepository} constructor parameter type at all.
 */
@Repository
public class JpaPatientRepository implements PatientRepository {

    private final PatientJpaRepository jpaRepository;

    public JpaPatientRepository(PatientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return jpaRepository.findById(id).map(JpaPatientRepository::toDomain);
    }

    @Override
    public void save(Patient patient) {
        jpaRepository.save(new PatientJpaEntity(
                patient.id(), patient.userId(), patient.region().name(), patient.fullName(), patient.dateOfBirth(), patient.gender(),
                patient.abhaId(), patient.createdAt(), patient.updatedAt()));
    }

    private static Patient toDomain(PatientJpaEntity entity) {
        return new Patient(
                entity.getId(), entity.getUserId(), Region.valueOf(entity.getRegion()), entity.getFullName(), entity.getDateOfBirth(),
                entity.getGender(), entity.getAbhaId(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
