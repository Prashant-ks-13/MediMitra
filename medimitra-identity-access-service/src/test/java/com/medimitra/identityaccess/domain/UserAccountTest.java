package com.medimitra.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    @Test
    void rejectsMissingEmail() {
        assertThrows(
                NullPointerException.class,
                () -> new UserAccount(UUID.randomUUID(), null, UserRole.PATIENT, Region.IN, Instant.now()));
    }

    @Test
    void rejectsMissingRole() {
        assertThrows(
                NullPointerException.class,
                () -> new UserAccount(UUID.randomUUID(), "a@b.com", null, Region.IN, Instant.now()));
    }
}
