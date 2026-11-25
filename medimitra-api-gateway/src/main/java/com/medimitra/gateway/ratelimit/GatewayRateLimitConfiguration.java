package com.medimitra.gateway.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.ClockProvider;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayRateLimitProperties.class)
public class GatewayRateLimitConfiguration {

    @Bean
    public GatewayRateLimiter gatewayRateLimiter(ClockProvider clockProvider, GatewayRateLimitProperties properties) {
        return new InMemoryGatewayRateLimiter(clockProvider, properties.getLimitPerWindow(), Duration.ofSeconds(properties.getWindowSeconds()));
    }

    @Bean
    public RateLimitingGlobalFilter rateLimitingGlobalFilter(GatewayRateLimiter rateLimiter, ClockProvider clockProvider, ObjectMapper objectMapper) {
        return new RateLimitingGlobalFilter(rateLimiter, clockProvider, objectMapper);
    }
}
