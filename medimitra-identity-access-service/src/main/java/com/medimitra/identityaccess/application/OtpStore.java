package com.medimitra.identityaccess.application;

import java.util.Optional;

/** Backs {@code /auth/otp}; production should wire a shared (Redis) implementation for multi-instance correctness. */
public interface OtpStore {
    void save(String email, OtpRecord record);

    Optional<OtpRecord> find(String email);

    void invalidate(String email);
}
