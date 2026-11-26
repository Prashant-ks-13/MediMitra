package com.medimitra.identityaccess.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.RandomIdGenerator;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.core.exception.DuplicateRequestException;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventPublisher;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserRegistrationServiceTest {

    private final ClockProvider clockProvider = new SystemClockProvider(Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
    private final IdGenerator idGenerator = new RandomIdGenerator();

    @Test
    void registersANewUserAndPublishesUserRegistered() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        KeycloakClient keycloakClient = mock(KeycloakClient.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        UUID keycloakUserId = UUID.randomUUID();
        when(repository.existsByEmail("a@b.com")).thenReturn(false);
        when(keycloakClient.createUser("a@b.com", "password123", UserRole.PATIENT, Region.IN)).thenReturn(keycloakUserId);

        UserRegistrationService service = new UserRegistrationService(repository, keycloakClient, eventPublisher, clockProvider, idGenerator);

        UUID result = service.register("a@b.com", "password123", UserRole.PATIENT, Region.IN);

        assertEquals(keycloakUserId, result);
        verify(repository).save(any());
        verify(eventPublisher).publish(any(EventEnvelope.class));
    }

    @Test
    void rejectsADuplicateEmailWithoutCallingKeycloak() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        KeycloakClient keycloakClient = mock(KeycloakClient.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        when(repository.existsByEmail("a@b.com")).thenReturn(true);

        UserRegistrationService service = new UserRegistrationService(repository, keycloakClient, eventPublisher, clockProvider, idGenerator);

        assertThrows(DuplicateRequestException.class, () -> service.register("a@b.com", "password123", UserRole.PATIENT, Region.IN));
        verify(keycloakClient, org.mockito.Mockito.never()).createUser(any(), any(), any(), any());
    }
}
