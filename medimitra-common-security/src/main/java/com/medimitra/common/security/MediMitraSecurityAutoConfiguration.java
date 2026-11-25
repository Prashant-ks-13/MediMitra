package com.medimitra.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.SystemClockProvider;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Wires the beans {@link EnableMediMitraSecurity} opts a service into; nothing here is registered otherwise. */
@Configuration
@EnableConfigurationProperties(MediMitraSecurityProperties.class)
public class MediMitraSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClockProvider clockProvider() {
        return new SystemClockProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenDenylist tokenDenylist() {
        return new NoOpTokenDenylist();
    }

    @Bean
    @ConditionalOnMissingBean
    public DpopReplayCache dpopReplayCache(ClockProvider clockProvider) {
        return new InMemoryDpopReplayCache(clockProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwkKeySource jwkKeySource(MediMitraSecurityProperties properties) {
        return new JwksKeyCache(URI.create(properties.getJwksUri()), Duration.ofSeconds(properties.getJwksMinRefreshIntervalSeconds()));
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenValidator jwtTokenValidator(
            JwkKeySource jwkKeySource, TokenDenylist tokenDenylist, ClockProvider clockProvider, MediMitraSecurityProperties properties) {
        return new JwtTokenValidator(
                jwkKeySource, tokenDenylist, clockProvider, properties.getIssuer(), properties.getAudience(),
                Duration.ofSeconds(properties.getClockSkewSeconds()));
    }

    @Bean
    @ConditionalOnMissingBean
    public DpopProofVerifier dpopProofVerifier(
            DpopReplayCache dpopReplayCache, ClockProvider clockProvider, MediMitraSecurityProperties properties) {
        return new DpopProofVerifier(dpopReplayCache, clockProvider, Duration.ofSeconds(properties.getDpopFreshnessSeconds()));
    }

    @Bean
    @ConditionalOnMissingBean
    public RegionGuard regionGuard(MediMitraSecurityProperties properties) {
        return new RegionGuard(Region.valueOf(properties.getRegion()));
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenValidator jwtTokenValidator,
            DpopProofVerifier dpopProofVerifier,
            RegionGuard regionGuard,
            ClockProvider clockProvider,
            ObjectMapper objectMapper) {
        return new JwtAuthenticationFilter(jwtTokenValidator, dpopProofVerifier, regionGuard, clockProvider, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceTokenProvider serviceTokenProvider(
            RestClient.Builder restClientBuilder, ClockProvider clockProvider, MediMitraSecurityProperties properties) {
        MediMitraSecurityProperties.ServiceToken config = properties.getServiceToken();
        return new CachingClientCredentialsServiceTokenProvider(
                restClientBuilder, config.getTokenUri(), config.getClientId(), config.getClientSecret(), clockProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsentClient consentClient(RestClient.Builder restClientBuilder, MediMitraSecurityProperties properties) {
        return new RestConsentClient(restClientBuilder, properties.getConsent().getBaseUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsentEnforcementAspect consentEnforcementAspect(ConsentClient consentClient) {
        return new ConsentEnforcementAspect(consentClient);
    }
}
