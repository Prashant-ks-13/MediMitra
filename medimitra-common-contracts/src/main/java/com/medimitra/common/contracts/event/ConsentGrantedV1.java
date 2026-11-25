package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published by Consent &amp; Access on {@code consent.granted}. Consumed by the gateway's access-decision
 * cache and the AI Assistant so both can refresh without polling.
 */
public record ConsentGrantedV1(
        UUID consentId,
        UUID patientId,
        UUID granteeId,
        String granteeType,
        List<String> scopes,
        String purpose,
        Instant validUntil,
        Instant grantedAt) {
}
