package com.medimitra.identityaccess.application;

import com.medimitra.common.contracts.event.UserRegisteredV1;
import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import com.medimitra.common.core.exception.DuplicateRequestException;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.identityaccess.IdentityErrorCodes;
import com.medimitra.identityaccess.application.port.KeycloakClient;
import com.medimitra.identityaccess.application.port.UserAccountRepository;
import com.medimitra.identityaccess.domain.UserAccount;
import java.time.Instant;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** Sign-up (README section 6): create the Keycloak user, record the local account, publish {@code user.registered}. */
public class UserRegistrationService {

    private final UserAccountRepository repository;
    private final KeycloakClient keycloakClient;
    private final EventPublisher eventPublisher;
    private final ClockProvider clockProvider;
    private final IdGenerator idGenerator;

    public UserRegistrationService(
            UserAccountRepository repository,
            KeycloakClient keycloakClient,
            EventPublisher eventPublisher,
            ClockProvider clockProvider,
            IdGenerator idGenerator) {
        this.repository = repository;
        this.keycloakClient = keycloakClient;
        this.eventPublisher = eventPublisher;
        this.clockProvider = clockProvider;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public UUID register(String email, String rawPassword, UserRole role, Region region) {
        if (repository.existsByEmail(email)) {
            throw new DuplicateRequestException(IdentityErrorCodes.EMAIL_ALREADY_REGISTERED, "email already registered: " + email);
        }
        UUID userId = keycloakClient.createUser(email, rawPassword, role, region);
        Instant now = clockProvider.now();
        repository.save(new UserAccount(userId, email, role, region, now));
        eventPublisher.publish(EventEnvelope.<UserRegisteredV1>builder()
                .eventId(idGenerator.newId())
                .eventType("registered")
                .aggregateType("user")
                .aggregateId(userId.toString())
                .region(region)
                .occurredAt(now)
                .producer("identity-access")
                .payload(new UserRegisteredV1(UserId.of(userId), role, region, now))
                .build());
        return userId;
    }
}
