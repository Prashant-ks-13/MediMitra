package com.medimitra.identityaccess.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code medimitra.identity.keycloak.*} — admin-API access, distinct from {@code medimitra.security.*} (token validation). */
@ConfigurationProperties(prefix = "medimitra.identity.keycloak")
public class IdentityAccessKeycloakProperties {

    private String baseUrl;
    private String realm;
    private String adminAudience;
    private String publicClientId;
    private String publicClientSecret;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getAdminAudience() {
        return adminAudience;
    }

    public void setAdminAudience(String adminAudience) {
        this.adminAudience = adminAudience;
    }

    public String getPublicClientId() {
        return publicClientId;
    }

    public void setPublicClientId(String publicClientId) {
        this.publicClientId = publicClientId;
    }

    public String getPublicClientSecret() {
        return publicClientSecret;
    }

    public void setPublicClientSecret(String publicClientSecret) {
        this.publicClientSecret = publicClientSecret;
    }
}
