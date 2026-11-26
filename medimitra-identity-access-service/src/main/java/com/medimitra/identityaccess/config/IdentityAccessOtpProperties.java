package com.medimitra.identityaccess.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code medimitra.identity.otp.*}. */
@ConfigurationProperties(prefix = "medimitra.identity.otp")
public class IdentityAccessOtpProperties {

    private long ttlSeconds = 300;
    private int codeLength = 6;

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }
}
