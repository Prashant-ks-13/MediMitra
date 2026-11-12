package com.medimitra.common.core;

import java.time.Clock;
import java.time.Instant;

/** Injected so time-dependent domain logic (holds, expiries, tokens) stays deterministic and testable. */
public interface ClockProvider {
    Instant now();

    Clock clock();
}
