package com.medimitra.gateway.tracing;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.core.RandomIdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayTracingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator() {
        return new RandomIdGenerator();
    }

    @Bean
    public CorrelationIdGlobalFilter correlationIdGlobalFilter(IdGenerator idGenerator) {
        return new CorrelationIdGlobalFilter(idGenerator);
    }

    @Bean
    public AccessLogGlobalFilter accessLogGlobalFilter(ClockProvider clockProvider) {
        return new AccessLogGlobalFilter(clockProvider);
    }
}
