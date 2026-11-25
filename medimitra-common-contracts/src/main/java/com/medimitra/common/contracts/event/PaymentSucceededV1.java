package com.medimitra.common.contracts.event;

import com.medimitra.common.core.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Published by Payment on {@code payment.succeeded}. {@code bookingReferenceId} is the appointment or
 * lab order this payment confirms; Scheduling/Lab Order consume it to confirm the held slot.
 */
public record PaymentSucceededV1(UUID paymentId, UUID bookingReferenceId, Money amount, String provider, Instant capturedAt) {
}
