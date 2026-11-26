package com.medimitra.identityaccess.application;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.identityaccess.IdentityErrorCodes;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

/** Backs {@code /auth/login} and {@code /auth/otp} (README section 6/8). */
public class AuthenticationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final KeycloakClient keycloakClient;
    private final UserAccountRepository userAccountRepository;
    private final OtpStore otpStore;
    private final OtpSender otpSender;
    private final ClockProvider clockProvider;
    private final Duration otpTtl;
    private final int otpLength;

    public AuthenticationService(
            KeycloakClient keycloakClient,
            UserAccountRepository userAccountRepository,
            OtpStore otpStore,
            OtpSender otpSender,
            ClockProvider clockProvider,
            Duration otpTtl,
            int otpLength) {
        this.keycloakClient = keycloakClient;
        this.userAccountRepository = userAccountRepository;
        this.otpStore = otpStore;
        this.otpSender = otpSender;
        this.clockProvider = clockProvider;
        this.otpTtl = otpTtl;
        this.otpLength = otpLength;
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
