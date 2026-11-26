package com.medimitra.identityaccess.application.port;

/** An access/refresh token pair minted by Keycloak (README section 8.1: 5-minute access token, rotating refresh token). */
public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {
}
