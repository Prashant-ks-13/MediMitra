package com.medimitra.common.core.error;

import java.util.Objects;
import java.util.regex.Pattern;

/** A stable code of the form {@code MM-<SERVICE>-<HTTP>-<REASON>}, e.g. {@code MM-SCH-409-SLOT_TAKEN}. */
public record ErrorCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("MM-[A-Z0-9]+-\\d{3}-[A-Z0-9_]+");

    public ErrorCode {
        Objects.requireNonNull(value, "value");
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("error code must match MM-<SERVICE>-<HTTP>-<REASON>: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
