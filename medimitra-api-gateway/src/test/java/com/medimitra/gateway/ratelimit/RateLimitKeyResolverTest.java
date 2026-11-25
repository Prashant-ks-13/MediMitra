package com.medimitra.gateway.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import com.medimitra.common.security.AuthenticatedPrincipal;
import com.medimitra.gateway.security.JwtDpopGlobalFilter;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class RateLimitKeyResolverTest {

    @Test
    void resolvesByAuthenticatedSubjectWhenPresent() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/patients/1").remoteAddress(new InetSocketAddress("9.9.9.9", 1234)));
        AuthenticatedPrincipal principal =
                new AuthenticatedPrincipal(UserId.of(userId), Set.of("PATIENT"), Set.of(), Region.IN, null, null, "jti", null, null);
        exchange.getAttributes().put(JwtDpopGlobalFilter.PRINCIPAL_ATTRIBUTE, principal);

        assertEquals("user:" + userId, RateLimitKeyResolver.resolve(exchange));
    }

    @Test
    void fallsBackToRemoteAddressWhenUnauthenticated() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/auth/login").remoteAddress(new InetSocketAddress("9.9.9.9", 1234)));

        assertEquals("ip:9.9.9.9", RateLimitKeyResolver.resolve(exchange));
    }
}
