package com.medimitra.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthenticatedPrincipalTest {

    @Test
    void mapsRolesToRolePrefixedAuthorities() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                UserId.of(UUID.randomUUID()), Set.of("DOCTOR"), Set.of("read"), Region.IN, "mfa", "sid-1", "jti-1", null, null);

        assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        assertFalse(principal.isActingOnBehalfOfUser());
    }

    @Test
    void flagsActingOnBehalfOfUser() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                UserId.of(UUID.randomUUID()), Set.of("PATIENT"), Set.of(), Region.EU, null, null, "jti-2", "thumbprint", "ai-assistant");

        assertTrue(principal.isActingOnBehalfOfUser());
    }
}
