package com.medimitra.identityaccess;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class IdentityAccessServiceApplicationTests {

    @Test
    void contextLoads() {
        // fails if any bean (security, messaging, Keycloak client, JPA) is wired incorrectly
    }
}
