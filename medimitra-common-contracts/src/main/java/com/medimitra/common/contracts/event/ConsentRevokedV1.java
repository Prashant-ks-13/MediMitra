package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Consent &amp; Access on {@code consent.revoked}; access must be denied from this instant on. */
public record ConsentRevokedV1(UUID consentId, UUID patientId, UUID revokedBy, String reason, Instant revokedAt) {
}
