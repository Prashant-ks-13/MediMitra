package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by AI Insights on {@code insight.generated}; Notification alerts the patient and caregivers. */
public record InsightGeneratedV1(
        UUID insightId,
        UUID patientId,
        String type,
        String severity,
        String suggestedSpecialty,
        String modelVersion,
        Instant generatedAt) {
}
