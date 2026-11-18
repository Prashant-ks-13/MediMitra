package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Document on {@code document.uploaded}; starts OCR and extraction in Document Intelligence. */
public record DocumentUploadedV1(UUID documentId, UUID patientId, UUID uploadedBy, String documentType, Instant uploadedAt) {
}
