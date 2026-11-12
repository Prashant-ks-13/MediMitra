package com.medimitra.common.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void addsSameCurrency() {
        Money a = new Money(1000, "INR");
        Money b = new Money(250, "INR");
        assertEquals(new Money(1250, "INR"), a.add(b));
    }

    @Test
    void rejectsCurrencyMismatch() {
        Money a = new Money(1000, "INR");
        Money b = new Money(250, "USD");
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }
}
