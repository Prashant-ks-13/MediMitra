package com.medimitra.gateway.ratelimit;

import com.medimitra.common.security.AuthenticatedPrincipal;
import com.medimitra.gateway.security.JwtDpopGlobalFilter;
import java.net.InetSocketAddress;
import org.springframework.web.server.ServerWebExchange;

/**
 * Keys by the authenticated subject when the JWT filter has already validated a token on this
 * exchange, else falls back to the remote address — so unauthenticated routes (login, OTP) are
 * still rate-limited per caller instead of sharing one unbounded bucket.
 */
public final class RateLimitKeyResolver {

    private RateLimitKeyResolver() {
    }

    public static String resolve(ServerWebExchange exchange) {
        AuthenticatedPrincipal principal = exchange.getAttribute(JwtDpopGlobalFilter.PRINCIPAL_ATTRIBUTE);
        if (principal != null) {
            return "user:" + principal.subject().value();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return "ip:" + (remoteAddress != null && remoteAddress.getAddress() != null ? remoteAddress.getAddress().getHostAddress() : "unknown");
    }
}
