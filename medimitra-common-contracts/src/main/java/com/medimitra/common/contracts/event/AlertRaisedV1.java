package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by AI Insights on {@code alert.raised} for a result that needs prompt attention. */
public record AlertRaisedV1(UUID alertId, UUID patientId, UUID insightId, String severity, String message, Instant raisedAt) {
}
