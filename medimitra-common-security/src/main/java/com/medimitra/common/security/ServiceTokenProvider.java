package com.medimitra.common.security;

/** Acquires a client-credentials token for service-to-service calls (section 8.5); background jobs use this, not a user's token. */
public interface ServiceTokenProvider {
    String acquireToken(String targetAudience);
}
