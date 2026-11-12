package com.medimitra.common.core;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) implements TypedId {

    public UserId {
        Objects.requireNonNull(value, "value");
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }

    public static UserId newId(IdGenerator generator) {
        return new UserId(generator.newId());
    }
}
