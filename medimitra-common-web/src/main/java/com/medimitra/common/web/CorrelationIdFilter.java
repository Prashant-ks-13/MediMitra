package com.medimitra.common.web;

import com.medimitra.common.core.IdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/** Propagates {@code X-MediMitra-Correlation-Id}: reuses an inbound value, else mints one, and always echoes it back. */
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-MediMitra-Correlation-Id";

    private final IdGenerator idGenerator;

    public CorrelationIdFilter(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = idGenerator.newId().toString();
        }
        MDC.put(CorrelationIdHolder.MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationIdHolder.MDC_KEY);
        }
    }
}
