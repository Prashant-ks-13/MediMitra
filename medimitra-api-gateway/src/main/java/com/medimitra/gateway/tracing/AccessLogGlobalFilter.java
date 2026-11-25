package com.medimitra.gateway.tracing;

import com.medimitra.common.core.ClockProvider;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Logs one line per request (method, path, status, duration, correlation id). Headers and bodies
 * are never logged — same rationale as medimitra-common-web's PII-free filter: the simplest way to
 * guarantee tokens and PII never reach the logs (section 8.3, rule 9).
 *
 * <p>Runs outermost (lowest order) so its measured duration covers every other gateway filter and
 * the actual backend call, and {@code doFinally} so it logs on error/cancel too, not just success.
 */
public class AccessLogGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger("medimitra.gateway.access");

    private final ClockProvider clockProvider;

    public AccessLogGlobalFilter(ClockProvider clockProvider) {
        this.clockProvider = clockProvider;
    }

    @Override
    public int getOrder() {
        return -200;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = clockProvider.now();
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long durationMs = Duration.between(start, clockProvider.now()).toMillis();
                    String correlationId = exchange.getAttribute(CorrelationIdGlobalFilter.CORRELATION_ID_ATTRIBUTE);
                    log.info(
                            "{} {} -> {} ({} ms) correlationId={}",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getPath().value(),
                            exchange.getResponse().getStatusCode(),
                            durationMs,
                            correlationId);
                });
    }
}
