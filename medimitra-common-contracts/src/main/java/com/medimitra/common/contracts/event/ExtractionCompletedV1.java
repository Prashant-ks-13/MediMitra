package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Document Intelligence on {@code extraction.completed}, before the patient confirms the values. */
public record ExtractionCompletedV1(
        UUID extractionId, UUID documentId, UUID patientId, double confidenceScore, Instant completedAt) {
}
