package com.medimitra.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.contracts.ApiError;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.common.core.exception.MediMitraException;
import com.medimitra.common.core.exception.TokenTamperedException;
import com.medimitra.common.security.AuthenticatedPrincipal;
import com.medimitra.common.security.DpopProofVerifier;
import com.medimitra.common.security.JwtTokenValidator;
import com.medimitra.common.security.RegionGuard;
import com.medimitra.common.security.SecurityErrorCodes;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Edge security gate (README section 3.4: "Gateway filter chain: JWT → DPoP → rate limit →
 * audit" / section 8.2). Re-validates the access token, verifies the DPoP proof for DPoP-bound
 * tokens, and checks the region claim matches this cell — before any route-specific handling runs.
 *
 * <p>Routes tagged with the {@value #PUBLIC_ROUTE_METADATA_KEY} metadata flag (login/OTP, which
 * must work without a token) skip all of this. Every other route requires a valid token: a
 * downstream service re-validates it again independently per the zero-trust principle — this
 * filter is a fail-fast layer, not the only line of defence.
 */
public class JwtDpopGlobalFilter implements GlobalFilter, Ordered {

    public static final String PUBLIC_ROUTE_METADATA_KEY = "medimitra.public";

    private static final Logger log = LoggerFactory.getLogger(JwtDpopGlobalFilter.class);

    private final JwtTokenValidator jwtTokenValidator;
    private final DpopProofVerifier dpopProofVerifier;
    private final RegionGuard regionGuard;
    private final ClockProvider clockProvider;
    private final ObjectMapper objectMapper;

    public JwtDpopGlobalFilter(
            JwtTokenValidator jwtTokenValidator,
            DpopProofVerifier dpopProofVerifier,
            RegionGuard regionGuard,
            ClockProvider clockProvider,
            ObjectMapper objectMapper) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.dpopProofVerifier = dpopProofVerifier;
        this.regionGuard = regionGuard;
        this.clockProvider = clockProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (isPublicRoute(exchange)) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return reject(exchange, new AuthenticationFailedException(SecurityErrorCodes.SIGNATURE_INVALID, "missing Authorization header"));
        }
        try {
            String token = extractToken(authorization);
            AuthenticatedPrincipal principal = jwtTokenValidator.validate(token);
            if (principal.dpopKeyThumbprint() != null) {
                String dpopHeader = request.getHeaders().getFirst("DPoP");
                if (dpopHeader == null) {
                    throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_MISSING, "token is DPoP-bound but no DPoP header was sent");
                }
                dpopProofVerifier.verify(dpopHeader, request.getMethod().name(), requestUriWithoutQuery(request), principal.dpopKeyThumbprint());
            }
            regionGuard.verify(principal.region());
        } catch (MediMitraException e) {
            return reject(exchange, e);
        }
        return chain.filter(exchange);
    }

    private boolean isPublicRoute(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route != null && Boolean.TRUE.equals(route.getMetadata().get(PUBLIC_ROUTE_METADATA_KEY));
    }

    private static String extractToken(String authorizationHeader) {
        int space = authorizationHeader.indexOf(' ');
        if (space < 0) {
            throw new AuthenticationFailedException(SecurityErrorCodes.SIGNATURE_INVALID, "malformed Authorization header");
        }
        return authorizationHeader.substring(space + 1);
    }

    private static String requestUriWithoutQuery(ServerHttpRequest request) {
        return UriComponentsBuilder.fromUri(request.getURI()).replaceQuery(null).build().toUriString();
    }

    private Mono<Void> reject(ServerWebExchange exchange, MediMitraException e) {
        log.warn("rejected request path={} code={} detail={}", exchange.getRequest().getPath().value(), e.errorCode(), e.getMessage());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.valueOf(e.httpStatus()));
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/problem+json");
        String genericMessage = e.httpStatus() == 401 ? "Authentication failed" : "Access denied";
        ApiError apiError = ApiError.of(
                e.errorCode().value(), e.httpStatus(), e.getClass().getSimpleName(), genericMessage,
                exchange.getRequest().getPath().value(), null, clockProvider.now());
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(apiError);
        } catch (JsonProcessingException ex) {
            bytes = ("{\"code\":\"" + e.errorCode().value() + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
