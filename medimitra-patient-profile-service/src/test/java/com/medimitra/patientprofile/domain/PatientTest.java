package com.medimitra.patientprofile.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.Region;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PatientTest {

    @Test
    void rejectsMissingUserId() {
        assertThrows(NullPointerException.class, () -> new Patient(UUID.randomUUID(), null, Region.IN, null, null, null, null, Instant.now(), Instant.now()));
    }

    @Test
    void newlyRegisteredHasNoDemographicsYet() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        Patient patient = Patient.newlyRegistered(id, userId, Region.IN, now);

        assertEquals(id, patient.id());
        assertEquals(userId, patient.userId());
        assertEquals(now, patient.createdAt());
        assertEquals(now, patient.updatedAt());
    }

    @Test
    void withDemographicsReplacesFieldsAndBumpsUpdatedAt() {
        Patient original = Patient.newlyRegistered(UUID.randomUUID(), UUID.randomUUID(), Region.IN, Instant.parse("2026-01-01T00:00:00Z"));
        Instant later = Instant.parse("2026-01-02T00:00:00Z");

        Patient updated = original.withDemographics("Asha Rao", java.time.LocalDate.of(1990, 1, 1), "FEMALE", "abha-1", later);

        assertEquals("Asha Rao", updated.fullName());
        assertEquals(later, updated.updatedAt());
        assertEquals(original.createdAt(), updated.createdAt());
    }

    @Test
    void isOwnedByComparesTheUserId() {
        UUID userId = UUID.randomUUID();
        Patient patient = Patient.newlyRegistered(UUID.randomUUID(), userId, Region.IN, Instant.now());

        assertTrue(patient.isOwnedBy(userId));
        assertFalse(patient.isOwnedBy(UUID.randomUUID()));
    }
}
