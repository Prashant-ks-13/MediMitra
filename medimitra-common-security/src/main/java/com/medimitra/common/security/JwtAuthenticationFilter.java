package com.medimitra.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.contracts.ApiError;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.common.core.exception.MediMitraException;
import com.medimitra.common.core.exception.TokenTamperedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Re-validates the bearer token on every request (section 8.2) — every service does this, not just the gateway. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;
    private final DpopProofVerifier dpopProofVerifier;
    private final RegionGuard regionGuard;
    private final ClockProvider clockProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String token = extractToken(authorization);
            AuthenticatedPrincipal principal = jwtTokenValidator.validate(token);
            if (principal.dpopKeyThumbprint() != null) {
                String dpopHeader = request.getHeader("DPoP");
                if (dpopHeader == null) {
                    throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_MISSING, "token is DPoP-bound but no DPoP header was sent");
                }
                dpopProofVerifier.verify(dpopHeader, request.getMethod(), request.getRequestURL().toString(), principal.dpopKeyThumbprint());
            }
            regionGuard.verify(principal.region());
            SecurityContextHolder.getContext().setAuthentication(principal);
            filterChain.doFilter(request, response);
        } catch (MediMitraException e) {
            writeError(response, e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static String extractToken(String authorizationHeader) {
        int space = authorizationHeader.indexOf(' ');
        if (space < 0) {
            throw new AuthenticationFailedException(SecurityErrorCodes.SIGNATURE_INVALID, "malformed Authorization header");
        }
        return authorizationHeader.substring(space + 1);
    }

    private void writeError(HttpServletResponse response, MediMitraException e) throws IOException {
        response.setStatus(e.httpStatus());
        response.setContentType("application/problem+json");
        ApiError apiError = ApiError.of(
                e.errorCode().value(), e.httpStatus(), e.getClass().getSimpleName(), e.getMessage(), null, null, clockProvider.now());
        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
