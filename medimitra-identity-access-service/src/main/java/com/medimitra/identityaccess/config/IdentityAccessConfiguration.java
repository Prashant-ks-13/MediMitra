package com.medimitra.identityaccess.config;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.common.security.ServiceTokenProvider;
import com.medimitra.identityaccess.application.KeycloakClient;
import com.medimitra.identityaccess.application.UserAccountRepository;
import com.medimitra.identityaccess.application.UserRegistrationService;
import com.medimitra.identityaccess.infrastructure.client.RestKeycloakClient;
import com.medimitra.identityaccess.infrastructure.persistence.JpaUserAccountRepository;
import com.medimitra.identityaccess.infrastructure.persistence.UserAccountJpaRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(IdentityAccessKeycloakProperties.class)
public class IdentityAccessConfiguration {

    @Bean
    public KeycloakClient keycloakClient(
            RestClient.Builder restClientBuilder, ServiceTokenProvider serviceTokenProvider, IdentityAccessKeycloakProperties properties) {
        return new RestKeycloakClient(
                restClientBuilder, properties.getBaseUrl(), serviceTokenProvider, properties.getRealm(), properties.getAdminAudience());
    }

    @Bean
    public UserAccountRepository userAccountRepository(UserAccountJpaRepository jpaRepository) {
        return new JpaUserAccountRepository(jpaRepository);
    }

    @Bean
    public UserRegistrationService userRegistrationService(
            UserAccountRepository repository, KeycloakClient keycloakClient, EventPublisher eventPublisher, ClockProvider clockProvider,
            IdGenerator idGenerator) {
        return new UserRegistrationService(repository, keycloakClient, eventPublisher, clockProvider, idGenerator);
    }
}
