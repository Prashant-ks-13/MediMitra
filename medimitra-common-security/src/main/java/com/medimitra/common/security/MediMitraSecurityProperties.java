package com.medimitra.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code medimitra.security.*}, per the common config-property naming convention. */
@ConfigurationProperties(prefix = "medimitra.security")
public class MediMitraSecurityProperties {

    private String issuer;
    private String jwksUri;
    private String audience;
    private String region;
    private long clockSkewSeconds = 30;
    private long dpopFreshnessSeconds = 60;
    private long jwksMinRefreshIntervalSeconds = 60;

    private final ServiceToken serviceToken = new ServiceToken();
    private final Consent consent = new Consent();

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getClockSkewSeconds() {
        return clockSkewSeconds;
    }

    public void setClockSkewSeconds(long clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }

    public long getDpopFreshnessSeconds() {
        return dpopFreshnessSeconds;
    }

    public void setDpopFreshnessSeconds(long dpopFreshnessSeconds) {
        this.dpopFreshnessSeconds = dpopFreshnessSeconds;
    }

    public long getJwksMinRefreshIntervalSeconds() {
        return jwksMinRefreshIntervalSeconds;
    }

    public void setJwksMinRefreshIntervalSeconds(long jwksMinRefreshIntervalSeconds) {
        this.jwksMinRefreshIntervalSeconds = jwksMinRefreshIntervalSeconds;
    }

    public ServiceToken getServiceToken() {
        return serviceToken;
    }

    public Consent getConsent() {
        return consent;
    }

    /** Client-credentials settings this service uses to call other services (section 8.5). */
    public static class ServiceToken {
        private String tokenUri;
        private String clientId;
        private String clientSecret;

        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    /** Where to reach Consent &amp; Access for {@code /access/check}. */
    public static class Consent {
        private String baseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
