package com.medimitra.common.security;

import com.medimitra.common.core.ClockProvider;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/** Caches each audience's token until shortly before it expires, so background jobs don't fetch one per call. */
public class CachingClientCredentialsServiceTokenProvider implements ServiceTokenProvider {

    private record CachedToken(String value, Instant expiresAt) {
    }

    private record TokenResponse(String access_token, int expires_in) {
    }

    private final RestClient restClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final ClockProvider clockProvider;
    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public CachingClientCredentialsServiceTokenProvider(
            RestClient.Builder restClientBuilder, String tokenUri, String clientId, String clientSecret, ClockProvider clockProvider) {
        this.restClient = restClientBuilder.build();
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clockProvider = clockProvider;
    }

    @Override
    public String acquireToken(String targetAudience) {
        Instant now = clockProvider.now();
        CachedToken cached = cache.get(targetAudience);
        if (cached != null && now.isBefore(cached.expiresAt())) {
            return cached.value();
        }
        TokenResponse response = restClient
                .post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formBody(targetAudience))
                .retrieve()
                .body(TokenResponse.class);
        Instant expiresAt = now.plusSeconds(Math.max(0, response.expires_in() - 10));
        cache.put(targetAudience, new CachedToken(response.access_token(), expiresAt));
        return response.access_token();
    }

    private String formBody(String targetAudience) {
        return "grant_type=client_credentials"
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&audience=" + encode(targetAudience);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
