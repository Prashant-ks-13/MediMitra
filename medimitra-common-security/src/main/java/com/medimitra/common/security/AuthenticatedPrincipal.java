package com.medimitra.common.security;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import java.util.Collection;
import java.util.Set;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * The validated caller for the current request, set as the {@code Authentication} so
 * {@code @PreAuthorize}/{@code hasRole} work exactly as with any other Spring Security principal.
 */
public final class AuthenticatedPrincipal extends AbstractAuthenticationToken {

    private final UserId subject;
    private final Set<String> roles;
    private final Set<String> scopes;
    private final Region region;
    private final String acr;
    private final String sid;
    private final String jti;
    private final String dpopKeyThumbprint;
    private final String actingOnBehalfOf;

    public AuthenticatedPrincipal(
            UserId subject,
            Set<String> roles,
            Set<String> scopes,
            Region region,
            String acr,
            String sid,
            String jti,
            String dpopKeyThumbprint,
            String actingOnBehalfOf) {
        super(toAuthorities(roles));
        this.subject = subject;
        this.roles = Set.copyOf(roles);
        this.scopes = Set.copyOf(scopes);
        this.region = region;
        this.acr = acr;
        this.sid = sid;
        this.jti = jti;
        this.dpopKeyThumbprint = dpopKeyThumbprint;
        this.actingOnBehalfOf = actingOnBehalfOf;
        setAuthenticated(true);
    }

    private static Collection<GrantedAuthority> toAuthorities(Set<String> roles) {
        return roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return subject;
    }

    public UserId subject() {
        return subject;
    }

    public Set<String> roles() {
        return roles;
    }

    public Set<String> scopes() {
        return scopes;
    }

    public Region region() {
        return region;
    }

    public String acr() {
        return acr;
    }

    public String sid() {
        return sid;
    }

    public String jti() {
        return jti;
    }

    public String dpopKeyThumbprint() {
        return dpopKeyThumbprint;
    }

    /** RFC 8693 {@code act.sub}: set when the AI assistant is calling on behalf of this user. */
    public String actingOnBehalfOf() {
        return actingOnBehalfOf;
    }

    public boolean isActingOnBehalfOfUser() {
        return actingOnBehalfOf != null;
    }
}
