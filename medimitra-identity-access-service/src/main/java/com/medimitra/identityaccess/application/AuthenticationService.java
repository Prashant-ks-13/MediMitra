package com.medimitra.identityaccess.application;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.common.security.TokenDenylist;
import com.medimitra.identityaccess.IdentityErrorCodes;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

/** Backs {@code /auth/login}, {@code /auth/otp}, {@code /auth/refresh}, {@code /auth/logout} and {@code /auth/step-up} (README section 6/8). */
public class AuthenticationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final KeycloakClient keycloakClient;
    private final UserAccountRepository userAccountRepository;
    private final OtpStore otpStore;
    private final OtpSender otpSender;
    private final TokenDenylist tokenDenylist;
    private final ClockProvider clockProvider;
    private final Duration otpTtl;
    private final int otpLength;
    private final Duration accessTokenLifetime;

    public AuthenticationService(
            KeycloakClient keycloakClient,
            UserAccountRepository userAccountRepository,
            OtpStore otpStore,
            OtpSender otpSender,
            TokenDenylist tokenDenylist,
            ClockProvider clockProvider,
            Duration otpTtl,
            int otpLength,
            Duration accessTokenLifetime) {
        this.keycloakClient = keycloakClient;
        this.userAccountRepository = userAccountRepository;
        this.otpStore = otpStore;
        this.otpSender = otpSender;
        this.tokenDenylist = tokenDenylist;
        this.clockProvider = clockProvider;
        this.otpTtl = otpTtl;
        this.otpLength = otpLength;
        this.accessTokenLifetime = accessTokenLifetime;
    }

    /** Proxies Keycloak's password grant as-is; Keycloak's own realm policy decides whether MFA is required. */
    public TokenResponse login(String email, String rawPassword, String dpopProof) {
        return keycloakClient.passwordLogin(email, rawPassword, dpopProof);
    }

    /** Silently no-ops for an unknown email, so this endpoint never confirms whether an account exists. */
    public void requestOtp(String email) {
        if (!userAccountRepository.existsByEmail(email)) {
            return;
        }
        String code = generateCode();
        otpStore.save(email, new OtpRecord(hash(code), clockProvider.now().plus(otpTtl)));
        otpSender.send(email, code);
    }

    public TokenResponse verifyOtp(String email, String code, String dpopProof) {
        OtpRecord record = otpStore
                .find(email)
                .orElseThrow(() -> new AuthenticationFailedException(IdentityErrorCodes.OTP_INVALID_OR_EXPIRED, "no OTP pending for " + email));
        boolean expired = clockProvider.now().isAfter(record.expiresAt());
        boolean mismatched = !hash(code).equals(record.hashedCode());
        if (expired || mismatched) {
            throw new AuthenticationFailedException(IdentityErrorCodes.OTP_INVALID_OR_EXPIRED, "OTP invalid or expired for " + email);
        }
        otpStore.invalidate(email);
        UUID userId = userAccountRepository
                .findIdByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException(IdentityErrorCodes.OTP_INVALID_OR_EXPIRED, "no account for " + email));
        return keycloakClient.tokenExchangeFor(userId, dpopProof);
    }

    /**
     * Rotation with reuse detection (section 8.4) is a Keycloak realm setting (Revoke Refresh
     * Token), not application logic — this just proxies whatever Keycloak decides.
     */
    public TokenResponse refresh(String refreshToken, String dpopProof) {
        return keycloakClient.refreshToken(refreshToken, dpopProof);
    }

    /**
     * Ends the Keycloak session, then denies {@code jti}/{@code sid} until the access token's
     * known fixed lifetime elapses (section 8.1: 5 minutes for all users) — the caller's current
     * token stays technically unexpired but is now rejected by every service's denylist check.
     */
    public void logout(String refreshToken, String jti, String sid) {
        keycloakClient.logout(refreshToken);
        tokenDenylist.deny(jti, sid, clockProvider.now().plus(accessTokenLifetime));
    }

    /**
     * Step-up re-authentication (section 8.4: acr=mfa within 5 minutes, required before pay,
     * consent changes, data export or deletion). Re-uses the OTP mechanism as the step-up factor;
     * a Keycloak protocol mapper on the token-exchange client is responsible for marking the
     * resulting token's {@code acr} claim as {@code mfa} — that mapping is realm configuration,
     * not something this call can dictate through the token-exchange request itself.
     */
    public TokenResponse stepUp(String email, String otpCode, String dpopProof) {
        return verifyOtp(email, otpCode, dpopProof);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, otpLength);
        int value = RANDOM.nextInt(bound);
        return String.format("%0" + otpLength + "d", value);
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
