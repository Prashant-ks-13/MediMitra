package com.medimitra.gateway.tracing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.SystemClockProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class AccessLogGlobalFilterTest {

    @Test
    void logsAndCompletesOnSuccess() {
        AccessLogGlobalFilter filter = new AccessLogGlobalFilter(new SystemClockProvider(Clock.fixed(Instant.now(), ZoneOffset.UTC)));
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/patients/1"));
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        }).block();

        assertTrue(chainCalled.get());
    }

    @Test
    void doesNotSwallowDownstreamErrors() {
        AccessLogGlobalFilter filter = new AccessLogGlobalFilter(new SystemClockProvider(Clock.fixed(Instant.now(), ZoneOffset.UTC)));
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/patients/1"));

        assertThrows(
                RuntimeException.class,
                () -> filter.filter(exchange, ex -> Mono.error(new RuntimeException("boom"))).block());
    }
}
