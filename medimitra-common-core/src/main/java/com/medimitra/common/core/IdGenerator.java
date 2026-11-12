package com.medimitra.common.core;

import java.util.UUID;

/** Injected so id-dependent domain logic stays deterministic and testable. */
public interface IdGenerator {
    UUID newId();
}
