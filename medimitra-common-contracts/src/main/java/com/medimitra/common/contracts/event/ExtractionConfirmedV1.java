package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Document Intelligence on {@code extraction.confirmed}; Medical Records writes the observations. */
public record ExtractionConfirmedV1(
        UUID extractionId, UUID documentId, UUID patientId, UUID confirmedBy, Instant confirmedAt) {
}
