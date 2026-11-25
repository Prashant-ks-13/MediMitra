package com.medimitra.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.security.DpopProofVerifier;
import com.medimitra.common.security.DpopReplayCache;
import com.medimitra.common.security.InMemoryDpopReplayCache;
import com.medimitra.common.security.JwkKeySource;
import com.medimitra.common.security.JwksKeyCache;
import com.medimitra.common.security.JwtTokenValidator;
import com.medimitra.common.security.MediMitraSecurityProperties;
import com.medimitra.common.security.NoOpTokenDenylist;
import com.medimitra.common.security.RegionGuard;
import com.medimitra.common.security.TokenDenylist;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires only the framework-free validators from medimitra-common-security directly. That module's
 * own auto-configuration (and its {@code JwtAuthenticationFilter}, which extends the servlet
 * {@code OncePerRequestFilter}) targets Spring MVC services; the gateway runs on WebFlux/Netty, so
 * importing it here would try to load a servlet class with no servlet API on this classpath.
 */
@Configuration
@EnableConfigurationProperties(MediMitraSecurityProperties.class)
public class GatewaySecurityConfiguration {

    @Bean
    public ClockProvider clockProvider() {
        return new SystemClockProvider();
    }

    @Bean
    public TokenDenylist tokenDenylist() {
        return new NoOpTokenDenylist();
    }

    @Bean
    public DpopReplayCache dpopReplayCache(ClockProvider clockProvider) {
        return new InMemoryDpopReplayCache(clockProvider);
    }

    @Bean
    public JwkKeySource jwkKeySource(MediMitraSecurityProperties properties) {
        return new JwksKeyCache(URI.create(properties.getJwksUri()), Duration.ofSeconds(properties.getJwksMinRefreshIntervalSeconds()));
    }

    @Bean
    public JwtTokenValidator jwtTokenValidator(
            JwkKeySource jwkKeySource, TokenDenylist tokenDenylist, ClockProvider clockProvider, MediMitraSecurityProperties properties) {
        return new JwtTokenValidator(
                jwkKeySource, tokenDenylist, clockProvider, properties.getIssuer(), properties.getAudience(),
                Duration.ofSeconds(properties.getClockSkewSeconds()));
    }

    @Bean
    public DpopProofVerifier dpopProofVerifier(
            DpopReplayCache dpopReplayCache, ClockProvider clockProvider, MediMitraSecurityProperties properties) {
        return new DpopProofVerifier(dpopReplayCache, clockProvider, Duration.ofSeconds(properties.getDpopFreshnessSeconds()));
    }

    @Bean
    public RegionGuard regionGuard(MediMitraSecurityProperties properties) {
        return new RegionGuard(Region.valueOf(properties.getRegion()));
    }

    @Bean
    public JwtDpopGlobalFilter jwtDpopGlobalFilter(
            JwtTokenValidator jwtTokenValidator,
            DpopProofVerifier dpopProofVerifier,
            RegionGuard regionGuard,
            ClockProvider clockProvider,
            ObjectMapper objectMapper) {
        return new JwtDpopGlobalFilter(jwtTokenValidator, dpopProofVerifier, regionGuard, clockProvider, objectMapper);
    }
}
