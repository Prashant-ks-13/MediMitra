package com.medimitra.patientprofile.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.Region;
import com.medimitra.patientprofile.domain.Patient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/** Same isolation technique as identity-access-service's equivalent test — see that class's Javadoc for why. */
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = JpaPatientRepositoryTest.TestConfig.class)
class JpaPatientRepositoryTest {

    @SpringBootApplication
    static class TestConfig {
    }

    @Autowired
    private PatientJpaRepository jpaRepository;

    @Test
    void existsByUserIdReflectsSavedPatients() {
        JpaPatientRepository repository = new JpaPatientRepository(jpaRepository);
        UUID userId = UUID.randomUUID();
        assertFalse(repository.existsByUserId(userId));

        repository.save(Patient.newlyRegistered(UUID.randomUUID(), userId, Region.IN, Instant.now()));

        assertTrue(repository.existsByUserId(userId));
    }

    @Test
    void findByIdRoundTripsTheDomainRecord() {
        JpaPatientRepository repository = new JpaPatientRepository(jpaRepository);
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Patient patient = Patient.newlyRegistered(id, UUID.randomUUID(), Region.EU, now)
                .withDemographics("Asha Rao", LocalDate.of(1990, 1, 1), "FEMALE", "abha-1", now);
        repository.save(patient);

        Patient found = repository.findById(id).orElseThrow();

        assertEquals(patient.id(), found.id());
        assertEquals(patient.userId(), found.userId());
        assertEquals(patient.region(), found.region());
        assertEquals(patient.fullName(), found.fullName());
        assertEquals(patient.dateOfBirth(), found.dateOfBirth());
    }
}
