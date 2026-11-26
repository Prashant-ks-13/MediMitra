package com.medimitra.patientprofile.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medimitra.common.core.RandomIdGenerator;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.core.exception.ConsentRequiredException;
import com.medimitra.common.core.exception.ResourceNotFoundException;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.patientprofile.application.port.PatientRepository;
import com.medimitra.patientprofile.domain.Patient;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PatientProfileServiceTest {

    private final SystemClockProvider clockProvider = new SystemClockProvider(Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void ownerCanReadTheirOwnProfile() {
        PatientRepository repository = mock(PatientRepository.class);
        UUID patientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Patient patient = Patient.newlyRegistered(patientId, userId, Region.IN, clockProvider.now());
        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        PatientProfileService service = new PatientProfileService(repository, mock(EventPublisher.class), clockProvider, new RandomIdGenerator());

        assertEquals(patient, service.getProfile(patientId, userId));
    }

    @Test
    void rejectsANonOwnerReadingTheProfile() {
        PatientRepository repository = mock(PatientRepository.class);
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.newlyRegistered(patientId, UUID.randomUUID(), Region.IN, clockProvider.now());
        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        PatientProfileService service = new PatientProfileService(repository, mock(EventPublisher.class), clockProvider, new RandomIdGenerator());

        assertThrows(ConsentRequiredException.class, () -> service.getProfile(patientId, UUID.randomUUID()));
    }

    @Test
    void rejectsAnUnknownPatientId() {
        PatientRepository repository = mock(PatientRepository.class);
        when(repository.findById(any())).thenReturn(Optional.empty());
        PatientProfileService service = new PatientProfileService(repository, mock(EventPublisher.class), clockProvider, new RandomIdGenerator());

        assertThrows(ResourceNotFoundException.class, () -> service.getProfile(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void updateProfileSavesAndPublishesPatientUpdated() {
        PatientRepository repository = mock(PatientRepository.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        UUID patientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Patient patient = Patient.newlyRegistered(patientId, userId, Region.IN, clockProvider.now());
        when(repository.findById(patientId)).thenReturn(Optional.of(patient));
        PatientProfileService service = new PatientProfileService(repository, eventPublisher, clockProvider, new RandomIdGenerator());

        Patient updated = service.updateProfile(patientId, userId, "Asha Rao", LocalDate.of(1990, 1, 1), "FEMALE", "abha-1");

        assertEquals("Asha Rao", updated.fullName());
        verify(repository).save(updated);
        verify(eventPublisher).publish(any(EventEnvelope.class));
    }
}
