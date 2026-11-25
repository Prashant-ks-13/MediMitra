package com.medimitra.gateway.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.SystemClockProvider;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class RateLimitingGlobalFilterTest {

    @Test
    void allowsUpToTheLimitThenReturns429() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SystemClockProvider clockProvider = new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC));
        InMemoryGatewayRateLimiter limiter = new InMemoryGatewayRateLimiter(clockProvider, 2, Duration.ofSeconds(60));
        RateLimitingGlobalFilter filter = new RateLimitingGlobalFilter(limiter, clockProvider, new ObjectMapper());

        AtomicInteger chainCallCount = new AtomicInteger();
        GatewayFilterChain chain = ex -> {
            chainCallCount.incrementAndGet();
            return Mono.empty();
        };

        for (int i = 0; i < 2; i++) {
            ServerWebExchange exchange = exchangeFromIp("1.2.3.4");
            filter.filter(exchange, chain).block();
        }
        assertEquals(2, chainCallCount.get());

        ServerWebExchange thirdExchange = exchangeFromIp("1.2.3.4");
        filter.filter(thirdExchange, chain).block();

        assertEquals(2, chainCallCount.get());
        assertEquals(429, thirdExchange.getResponse().getStatusCode().value());
    }

    @Test
    void differentCallersAreNotThrottledByOneAnother() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SystemClockProvider clockProvider = new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC));
        InMemoryGatewayRateLimiter limiter = new InMemoryGatewayRateLimiter(clockProvider, 1, Duration.ofSeconds(60));
        RateLimitingGlobalFilter filter = new RateLimitingGlobalFilter(limiter, clockProvider, new ObjectMapper());
        AtomicInteger chainCallCount = new AtomicInteger();
        GatewayFilterChain chain = ex -> {
            chainCallCount.incrementAndGet();
            return Mono.empty();
        };

        filter.filter(exchangeFromIp("1.2.3.4"), chain).block();
        filter.filter(exchangeFromIp("5.6.7.8"), chain).block();

        assertEquals(2, chainCallCount.get());
    }

    private static ServerWebExchange exchangeFromIp(String ip) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/patients/1").remoteAddress(new InetSocketAddress(ip, 1234)));
    }
}
