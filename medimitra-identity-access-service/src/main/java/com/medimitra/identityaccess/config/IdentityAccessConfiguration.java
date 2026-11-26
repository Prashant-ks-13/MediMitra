package com.medimitra.identityaccess.config;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.common.security.ServiceTokenProvider;
import com.medimitra.common.security.TokenDenylist;
import com.medimitra.identityaccess.application.AuthenticationService;
import com.medimitra.identityaccess.application.UserProfileService;
import com.medimitra.identityaccess.application.UserRegistrationService;
import com.medimitra.identityaccess.application.port.KeycloakClient;
import com.medimitra.identityaccess.application.port.OtpSender;
import com.medimitra.identityaccess.application.port.OtpStore;
import com.medimitra.identityaccess.application.port.UserAccountRepository;
import com.medimitra.identityaccess.infrastructure.client.RestKeycloakClient;
import com.medimitra.identityaccess.infrastructure.otp.InMemoryOtpStore;
import com.medimitra.identityaccess.infrastructure.otp.LoggingOtpSender;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({IdentityAccessKeycloakProperties.class, IdentityAccessOtpProperties.class})
public class IdentityAccessConfiguration {

    @Bean
    public KeycloakClient keycloakClient(
            RestClient.Builder restClientBuilder, ServiceTokenProvider serviceTokenProvider, IdentityAccessKeycloakProperties properties) {
        return new RestKeycloakClient(
                restClientBuilder, properties.getBaseUrl(), serviceTokenProvider, properties.getRealm(), properties.getAdminAudience(),
                properties.getPublicClientId(), properties.getPublicClientSecret());
    }

    // UserAccountRepository itself is not wired here — its JPA adapter is @Repository-annotated
    // and self-registering.

    @Bean
    public UserRegistrationService userRegistrationService(
            UserAccountRepository repository, KeycloakClient keycloakClient, EventPublisher eventPublisher, ClockProvider clockProvider,
            IdGenerator idGenerator) {
        return new UserRegistrationService(repository, keycloakClient, eventPublisher, clockProvider, idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public OtpStore otpStore() {
        return new InMemoryOtpStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public OtpSender otpSender() {
        return new LoggingOtpSender();
    }

    @Bean
    public AuthenticationService authenticationService(
            KeycloakClient keycloakClient,
            UserAccountRepository userAccountRepository,
            OtpStore otpStore,
            OtpSender otpSender,
            TokenDenylist tokenDenylist,
            ClockProvider clockProvider,
            IdentityAccessOtpProperties otpProperties,
            IdentityAccessKeycloakProperties keycloakProperties) {
        return new AuthenticationService(
                keycloakClient, userAccountRepository, otpStore, otpSender, tokenDenylist, clockProvider,
                Duration.ofSeconds(otpProperties.getTtlSeconds()), otpProperties.getCodeLength(),
                Duration.ofSeconds(keycloakProperties.getAccessTokenLifetimeSeconds()));
    }

    @Bean
    public UserProfileService userProfileService(UserAccountRepository repository) {
        return new UserProfileService(repository);
    }
}
