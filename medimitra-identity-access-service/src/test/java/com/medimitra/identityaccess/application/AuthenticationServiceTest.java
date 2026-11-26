package com.medimitra.identityaccess.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.common.security.TokenDenylist;
import com.medimitra.identityaccess.application.port.KeycloakClient;
import com.medimitra.identityaccess.application.port.OtpRecord;
import com.medimitra.identityaccess.application.port.OtpSender;
import com.medimitra.identityaccess.application.port.OtpStore;
import com.medimitra.identityaccess.application.port.TokenResponse;
import com.medimitra.identityaccess.application.port.UserAccountRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {

    private final ClockProvider clockProvider = new SystemClockProvider(Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    private AuthenticationService service(
            KeycloakClient keycloakClient, UserAccountRepository repository, OtpStore otpStore, OtpSender otpSender, TokenDenylist tokenDenylist) {
        return new AuthenticationService(
                keycloakClient, repository, otpStore, otpSender, tokenDenylist, clockProvider, Duration.ofMinutes(5), 6,
                Duration.ofMinutes(5));
    }

    @Test
    void loginDelegatesDirectlyToKeycloak() {
        KeycloakClient keycloakClient = mock(KeycloakClient.class);
        TokenResponse expected = new TokenResponse("a", "b", 300, "DPoP");
        when(keycloakClient.passwordLogin("a@b.com", "pw", "proof")).thenReturn(expected);
        AuthenticationService service = service(
                keycloakClient, mock(UserAccountRepository.class), mock(OtpStore.class), mock(OtpSender.class), mock(TokenDenylist.class));

        assertEquals(expected, service.login("a@b.com", "pw", "proof"));
    }

    @Test
    void requestOtpSkipsSilentlyForUnknownEmail() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        OtpSender otpSender = mock(OtpSender.class);
        when(repository.existsByEmail("unknown@b.com")).thenReturn(false);
        AuthenticationService service =
                service(mock(KeycloakClient.class), repository, mock(OtpStore.class), otpSender, mock(TokenDenylist.class));

        service.requestOtp("unknown@b.com");

        verify(otpSender, org.mockito.Mockito.never()).send(any(), any());
    }

    @Test
    void requestOtpStoresAndSendsACodeForAKnownEmail() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        OtpStore otpStore = mock(OtpStore.class);
        OtpSender otpSender = mock(OtpSender.class);
        when(repository.existsByEmail("a@b.com")).thenReturn(true);
        AuthenticationService service = service(mock(KeycloakClient.class), repository, otpStore, otpSender, mock(TokenDenylist.class));

        service.requestOtp("a@b.com");

        verify(otpStore).save(eq("a@b.com"), any());
        verify(otpSender).send(eq("a@b.com"), any());
    }

    @Test
    void verifyOtpExchangesTokensOnAMatchingCode() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        OtpStore otpStore = mock(OtpStore.class);
        KeycloakClient keycloakClient = mock(KeycloakClient.class);
        UUID userId = UUID.randomUUID();
        OtpSender otpSender = mock(OtpSender.class);
        AuthenticationService service = service(keycloakClient, repository, otpStore, otpSender, mock(TokenDenylist.class));

        // capture the real generated code by stubbing send(), then verify with that exact code
        org.mockito.ArgumentCaptor<String> codeCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        when(repository.existsByEmail("a@b.com")).thenReturn(true);
        service.requestOtp("a@b.com");
        verify(otpSender).send(eq("a@b.com"), codeCaptor.capture());
        String realCode = codeCaptor.getValue();

        org.mockito.ArgumentCaptor<OtpRecord> recordCaptor = org.mockito.ArgumentCaptor.forClass(OtpRecord.class);
        verify(otpStore).save(eq("a@b.com"), recordCaptor.capture());
        when(otpStore.find("a@b.com")).thenReturn(Optional.of(recordCaptor.getValue()));
        when(repository.findIdByEmail("a@b.com")).thenReturn(Optional.of(userId));
        TokenResponse expected = new TokenResponse("x", "y", 300, "DPoP");
        when(keycloakClient.tokenExchangeFor(userId, null)).thenReturn(expected);

        TokenResponse result = service.verifyOtp("a@b.com", realCode, null);

        assertEquals(expected, result);
        verify(otpStore).invalidate("a@b.com");
    }

    @Test
    void verifyOtpRejectsAWrongCode() {
        OtpStore otpStore = mock(OtpStore.class);
        when(otpStore.find("a@b.com")).thenReturn(Optional.of(new OtpRecord("some-other-hash", clockProvider.now().plusSeconds(60))));
        AuthenticationService service =
                service(mock(KeycloakClient.class), mock(UserAccountRepository.class), otpStore, mock(OtpSender.class), mock(TokenDenylist.class));

        assertThrows(AuthenticationFailedException.class, () -> service.verifyOtp("a@b.com", "000000", null));
    }

    @Test
    void verifyOtpRejectsAnExpiredCode() {
        OtpStore otpStore = mock(OtpStore.class);
        when(otpStore.find("a@b.com")).thenReturn(Optional.of(new OtpRecord(sha256("000000"), clockProvider.now().minusSeconds(1))));
        AuthenticationService service =
                service(mock(KeycloakClient.class), mock(UserAccountRepository.class), otpStore, mock(OtpSender.class), mock(TokenDenylist.class));

        assertThrows(AuthenticationFailedException.class, () -> service.verifyOtp("a@b.com", "000000", null));
    }

    @Test
    void refreshDelegatesDirectlyToKeycloak() {
        KeycloakClient keycloakClient = mock(KeycloakClient.class);
        TokenResponse expected = new TokenResponse("a", "b", 300, "DPoP");
        when(keycloakClient.refreshToken("old-refresh", "proof")).thenReturn(expected);
        AuthenticationService service = service(
                keycloakClient, mock(UserAccountRepository.class), mock(OtpStore.class), mock(OtpSender.class), mock(TokenDenylist.class));

        assertEquals(expected, service.refresh("old-refresh", "proof"));
    }

    @Test
    void logoutEndsTheKeycloakSessionAndDeniesTheCurrentToken() {
        KeycloakClient keycloakClient = mock(KeycloakClient.class);
        TokenDenylist tokenDenylist = mock(TokenDenylist.class);
        AuthenticationService service = service(
                keycloakClient, mock(UserAccountRepository.class), mock(OtpStore.class), mock(OtpSender.class), tokenDenylist);

        service.logout("refresh-1", "jti-1", "sid-1");

        verify(keycloakClient).logout("refresh-1");
        verify(tokenDenylist).deny("jti-1", "sid-1", clockProvider.now().plus(Duration.ofMinutes(5)));
    }

    @Test
    void stepUpReusesTheOtpVerificationMechanism() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        OtpStore otpStore = mock(OtpStore.class);
        KeycloakClient keycloakClient = mock(KeycloakClient.class);
        UUID userId = UUID.randomUUID();
        when(otpStore.find("a@b.com")).thenReturn(Optional.of(new OtpRecord(sha256("123456"), clockProvider.now().plusSeconds(60))));
        when(repository.findIdByEmail("a@b.com")).thenReturn(Optional.of(userId));
        TokenResponse expected = new TokenResponse("stepped-up", "refresh", 300, "DPoP");
        when(keycloakClient.tokenExchangeFor(userId, null)).thenReturn(expected);
        AuthenticationService service = service(keycloakClient, repository, otpStore, mock(OtpSender.class), mock(TokenDenylist.class));

        assertEquals(expected, service.stepUp("a@b.com", "123456", null));
    }

    private static String sha256(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
