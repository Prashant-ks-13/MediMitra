package com.medimitra.common.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StepUpGuardTest {

    @Test
    void passesWhenAcrMatches() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                UserId.of(UUID.randomUUID()), Set.of("PATIENT"), Set.of(), Region.IN, "mfa", null, "jti", null, null);
        assertDoesNotThrow(() -> StepUpGuard.require(principal, "mfa"));
    }

    @Test
    void rejectsWhenAcrMissing() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                UserId.of(UUID.randomUUID()), Set.of("PATIENT"), Set.of(), Region.IN, "pwd", null, "jti", null, null);
        assertThrows(AuthenticationFailedException.class, () -> StepUpGuard.require(principal, "mfa"));
    }
}
