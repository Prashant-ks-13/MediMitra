package com.medimitra.gateway.tracing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.medimitra.common.core.RandomIdGenerator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class CorrelationIdGlobalFilterTest {

    @Test
    void mintsAndForwardsAndEchoesAnIdWhenNoneSupplied() {
        CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter(new RandomIdGenerator());
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/patients/1"));
        AtomicReference<String> forwardedHeader = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            forwardedHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(forwardedHeader.get());
        assertEquals(forwardedHeader.get(), exchange.getResponse().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
        assertEquals(forwardedHeader.get(), exchange.getAttribute(CorrelationIdGlobalFilter.CORRELATION_ID_ATTRIBUTE));
    }

    @Test
    void reusesAndEchoesAnInboundId() {
        CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter(new RandomIdGenerator());
        ServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/patients/1").header(CorrelationIdGlobalFilter.HEADER, "corr-abc"));
        AtomicReference<String> forwardedHeader = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            forwardedHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertEquals("corr-abc", forwardedHeader.get());
        assertEquals("corr-abc", exchange.getResponse().getHeaders().getFirst(CorrelationIdGlobalFilter.HEADER));
    }
}
