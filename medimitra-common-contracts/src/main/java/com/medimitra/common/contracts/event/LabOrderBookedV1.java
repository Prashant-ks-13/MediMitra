package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Lab Order on {@code lab_order.booked}; notifies the lab and the patient. */
public record LabOrderBookedV1(
        UUID labOrderId, UUID patientId, UUID labId, String collectionType, Instant collectionAt, Instant bookedAt) {
}
