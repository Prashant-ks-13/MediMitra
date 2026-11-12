package com.medimitra.common.core;

import java.util.UUID;

public final class RandomIdGenerator implements IdGenerator {

    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
