package com.medimitra.common.core;

import java.util.Objects;

/** Amount in minor currency units, never a double, so rounding never corrupts a charge or refund. */
public record Money(long amountMinor, String currency) {

    public Money {
        Objects.requireNonNull(currency, "currency");
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be an ISO-4217 code: " + currency);
        }
    }

    public static Money zero(String currency) {
        return new Money(0L, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amountMinor + other.amountMinor, currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amountMinor - other.amountMinor, currency);
    }

    public boolean isNegative() {
        return amountMinor < 0;
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return amountMinor > other.amountMinor;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
