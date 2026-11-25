package com.medimitra.gateway.ratelimit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.contracts.ApiError;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.gateway.GatewayErrorCodes;
import com.medimitra.gateway.tracing.CorrelationIdGlobalFilter;
import java.nio.charset.StandardCharsets;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Runs right after JWT/DPoP validation (README section 3.4: "JWT → DPoP → rate limit → audit").
 * Keyed by {@link RateLimitKeyResolver} — authenticated subject when available, else remote IP,
 * so unauthenticated routes like login/OTP are rate-limited too.
 */
public class RateLimitingGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayRateLimiter rateLimiter;
    private final ClockProvider clockProvider;
    private final ObjectMapper objectMapper;

    public RateLimitingGlobalFilter(GatewayRateLimiter rateLimiter, ClockProvider clockProvider, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.clockProvider = clockProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = RateLimitKeyResolver.resolve(exchange);
        RateLimitDecision decision = rateLimiter.tryConsume(key);
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        if (!decision.allowed()) {
            return reject(exchange, decision);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> reject(ServerWebExchange exchange, RateLimitDecision decision) {
        String correlationId = exchange.getAttribute(CorrelationIdGlobalFilter.CORRELATION_ID_ATTRIBUTE);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/problem+json");
        response.getHeaders().add("Retry-After", String.valueOf(decision.retryAfterSeconds()));
        ApiError apiError = ApiError.of(
                GatewayErrorCodes.RATE_LIMIT_EXCEEDED.value(), 429, "TooManyRequests", "rate limit exceeded, retry later",
                exchange.getRequest().getPath().value(), correlationId, clockProvider.now());
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(apiError);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":\"" + GatewayErrorCodes.RATE_LIMIT_EXCEEDED.value() + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
