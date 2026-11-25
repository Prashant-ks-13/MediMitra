package com.medimitra.common.web;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.core.RandomIdGenerator;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.web.idempotency.IdempotencyFilter;
import com.medimitra.common.web.idempotency.IdempotencyInterceptor;
import com.medimitra.common.web.idempotency.IdempotencyStore;
import com.medimitra.common.web.idempotency.InMemoryIdempotencyStore;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Wires the beans {@link EnableMediMitraWeb} opts a service into; nothing here is registered otherwise. */
@Configuration
public class MediMitraWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClockProvider clockProvider() {
        return new SystemClockProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator() {
        return new RandomIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ClockProvider clockProvider) {
        return new GlobalExceptionHandler(clockProvider);
    }

    /** Runs first so every later filter and the exception handler see the correlation id in MDC. */
    @Bean
    public FilterRegistrationBean<Filter> correlationIdFilter(IdGenerator idGenerator) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(new CorrelationIdFilter(idGenerator));
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> piiMaskingRequestLoggingFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(new PiiMaskingRequestLoggingFilter());
        registration.setOrder(-90);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> idempotencyFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(new IdempotencyFilter());
        registration.setOrder(-80);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyStore idempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyInterceptor idempotencyInterceptor(IdempotencyStore idempotencyStore, ClockProvider clockProvider) {
        return new IdempotencyInterceptor(idempotencyStore, clockProvider);
    }

    @Bean
    public WebMvcConfigurer idempotencyWebMvcConfigurer(IdempotencyInterceptor idempotencyInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(idempotencyInterceptor);
            }
        };
    }
}
