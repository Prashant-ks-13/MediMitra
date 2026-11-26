package com.medimitra.identityaccess.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.exception.ResourceNotFoundException;
import com.medimitra.identityaccess.domain.UserAccount;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserProfileServiceTest {

    @Test
    void returnsTheAccountWhenFound() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        UUID userId = UUID.randomUUID();
        UserAccount account = new UserAccount(userId, "a@b.com", UserRole.PATIENT, Region.IN, Instant.now());
        when(repository.findById(userId)).thenReturn(Optional.of(account));
        UserProfileService service = new UserProfileService(repository);

        assertEquals(account, service.getProfile(userId));
    }

    @Test
    void rejectsAnUnknownUserId() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        UUID userId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());
        UserProfileService service = new UserProfileService(repository);

        assertThrows(ResourceNotFoundException.class, () -> service.getProfile(userId));
    }
}
