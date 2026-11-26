package com.medimitra.patientprofile.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medimitra.common.contracts.event.UserRegisteredV1;
import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.RandomIdGenerator;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.core.UserId;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.patientprofile.application.port.PatientRepository;
import com.medimitra.patientprofile.domain.Patient;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PatientCreationServiceTest {

    private final SystemClockProvider clockProvider = new SystemClockProvider(Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void createsAPatientForAPatientRoleRegistration() {
        PatientRepository repository = mock(PatientRepository.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        UUID userId = UUID.randomUUID();
        when(repository.existsByUserId(userId)).thenReturn(false);
        PatientCreationService service = new PatientCreationService(repository, eventPublisher, clockProvider, new RandomIdGenerator());

        service.createFromRegistration(new UserRegisteredV1(UserId.of(userId), UserRole.PATIENT, Region.IN, clockProvider.now()));

        verify(repository).save(any(Patient.class));
        verify(eventPublisher).publish(any(EventEnvelope.class));
    }

    @Test
    void ignoresNonPatientRoles() {
        PatientRepository repository = mock(PatientRepository.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        PatientCreationService service = new PatientCreationService(repository, eventPublisher, clockProvider, new RandomIdGenerator());

        service.createFromRegistration(
                new UserRegisteredV1(UserId.of(UUID.randomUUID()), UserRole.CAREGIVER, Region.IN, clockProvider.now()));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void doesNotDuplicateAnExistingPatient() {
        PatientRepository repository = mock(PatientRepository.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        UUID userId = UUID.randomUUID();
        when(repository.existsByUserId(userId)).thenReturn(true);
        PatientCreationService service = new PatientCreationService(repository, eventPublisher, clockProvider, new RandomIdGenerator());

        service.createFromRegistration(new UserRegisteredV1(UserId.of(userId), UserRole.PATIENT, Region.IN, clockProvider.now()));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
