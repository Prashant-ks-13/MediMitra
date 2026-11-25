package com.medimitra.common.contracts.event;

import com.medimitra.common.core.Money;
import java.time.Instant;
import java.util.UUID;

/** Published by Payment on {@code payment.failed}; the held slot is released rather than confirmed. */
public record PaymentFailedV1(
        UUID paymentId, UUID bookingReferenceId, Money amount, String provider, String failureReason, Instant failedAt) {
}
