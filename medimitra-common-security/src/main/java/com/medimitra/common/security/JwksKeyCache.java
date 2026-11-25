package com.medimitra.common.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Caches the JWKS fetched from {@code jwksUri}. On an unknown {@code kid} it refreshes at most
 * once per {@code minRefreshInterval} (section 8.3, rule 2) instead of hitting the issuer on every
 * unrecognised key; on a fetch failure it keeps serving the stale cache rather than failing every
 * request during a transient JWKS outage.
 */
public class JwksKeyCache implements JwkKeySource {

    private final URI jwksUri;
    private final HttpClient httpClient;
    private final Duration minRefreshInterval;

    private volatile JWKSet cached;
    private volatile Instant lastFetchedAt = Instant.EPOCH;

    public JwksKeyCache(URI jwksUri, Duration minRefreshInterval) {
        this.jwksUri = jwksUri;
        this.minRefreshInterval = minRefreshInterval;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public synchronized Optional<JWK> findByKeyId(String keyId, Instant now) {
        JWK found = (cached == null) ? null : cached.getKeyByKeyId(keyId);
        if (found == null && now.isAfter(lastFetchedAt.plus(minRefreshInterval))) {
            refresh(now);
            found = (cached == null) ? null : cached.getKeyByKeyId(keyId);
        }
        return Optional.ofNullable(found);
    }

    private void refresh(Instant now) {
        try {
            HttpRequest request = HttpRequest.newBuilder(jwksUri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                cached = JWKSet.parse(response.body());
            }
            lastFetchedAt = now;
        } catch (IOException | InterruptedException | ParseException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            lastFetchedAt = now;
        }
    }
}
