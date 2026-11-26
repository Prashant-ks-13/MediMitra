package com.medimitra.identityaccess.api;

import com.medimitra.identityaccess.application.TokenResponse;

public record TokenResponseDto(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {

    public static TokenResponseDto from(TokenResponse tokenResponse) {
        return new TokenResponseDto(
                tokenResponse.accessToken(), tokenResponse.refreshToken(), tokenResponse.expiresInSeconds(), tokenResponse.tokenType());
    }
}
