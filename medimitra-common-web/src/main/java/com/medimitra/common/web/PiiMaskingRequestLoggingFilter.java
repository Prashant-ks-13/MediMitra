package com.medimitra.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs one line per request (method, path, status, duration, correlation id). Headers and bodies
 * are never logged, which is the simplest way to guarantee tokens and PII never reach the logs
 * (section 8.3, rule 9) without having to maintain a list of fields to mask.
 */
public class PiiMaskingRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("medimitra.access");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info(
                    "{} {} -> {} ({} ms) correlationId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs, CorrelationIdHolder.current());
        }
    }
}
