package com.medimitra.identityaccess.application;

import java.time.Instant;

/** A one-time code's hash and expiry; the raw code itself is never stored. */
public record OtpRecord(String hashedCode, Instant expiresAt) {
}
