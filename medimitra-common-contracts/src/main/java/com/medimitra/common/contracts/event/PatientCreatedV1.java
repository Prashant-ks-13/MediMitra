package com.medimitra.common.contracts.event;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import java.time.Instant;
import java.util.UUID;

/** Published by Patient Profile on {@code patient.created} when a patient record is first created. */
public record PatientCreatedV1(UUID patientId, UserId userId, Region region, Instant createdAt) {
}
