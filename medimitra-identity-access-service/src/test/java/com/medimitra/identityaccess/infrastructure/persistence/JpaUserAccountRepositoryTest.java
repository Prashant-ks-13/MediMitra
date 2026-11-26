package com.medimitra.identityaccess.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.identityaccess.domain.UserAccount;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Deliberately points at a minimal nested {@code @SpringBootApplication} instead of the real
 * {@code IdentityAccessServiceApplication}: that one carries {@code @EnableMediMitraMessaging},
 * which would pull Kafka/outbox beans into this JPA-only test slice and fail to find them, since
 * {@code @DataJpaTest} only auto-configures a narrow JPA-related autoconfiguration set.
 */
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = JpaUserAccountRepositoryTest.TestConfig.class)
class JpaUserAccountRepositoryTest {

    @SpringBootApplication
    static class TestConfig {
    }

    @org.springframework.beans.factory.annotation.Autowired
    private UserAccountJpaRepository jpaRepository;

    @Test
    void existsByEmailReflectsSavedAccounts() {
        JpaUserAccountRepository repository = new JpaUserAccountRepository(jpaRepository);
        assertFalse(repository.existsByEmail("a@b.com"));

        repository.save(new UserAccount(UUID.randomUUID(), "a@b.com", UserRole.PATIENT, Region.IN, Instant.now()));

        assertTrue(repository.existsByEmail("a@b.com"));
    }
}
