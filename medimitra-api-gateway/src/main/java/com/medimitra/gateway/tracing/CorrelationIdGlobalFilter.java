package com.medimitra.gateway.tracing;

import com.medimitra.common.core.IdGenerator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Propagates {@code X-MediMitra-Correlation-Id} (README section 7.5): reuses an inbound value,
 * else mints one, forwards it to the downstream service, and always echoes it back to the client
 * — including on a rejection, so a 401/429 can still be traced back to a specific request.
 *
 * <p>Runs before every other gateway filter (lowest order) so the correlation id is available to
 * their logging via {@link #CORRELATION_ID_ATTRIBUTE}.
 */
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-MediMitra-Correlation-Id";
    public static final String CORRELATION_ID_ATTRIBUTE = CorrelationIdGlobalFilter.class.getName() + ".correlationId";

    private final IdGenerator idGenerator;

    public CorrelationIdGlobalFilter(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(HEADER);
        ServerWebExchange effectiveExchange = exchange;
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = idGenerator.newId().toString();
            ServerHttpRequest mutatedRequest = request.mutate().header(HEADER, correlationId).build();
            effectiveExchange = exchange.mutate().request(mutatedRequest).build();
        }
        // Headers must be set before the response commits, not after — the eventual response
        // write (a successful proxy or a rejection from a later filter) happens downstream of
        // this call, so setting it now is the only point guaranteed to be before that commit.
        exchange.getAttributes().put(CORRELATION_ID_ATTRIBUTE, correlationId);
        exchange.getResponse().getHeaders().add(HEADER, correlationId);
        return chain.filter(effectiveExchange);
    }
}
