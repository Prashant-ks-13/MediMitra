package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/** Base of every exception raised by MediMitra code, so {@code GlobalExceptionHandler} can map any of them to an {@code ApiError}. */
public abstract class MediMitraException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    protected MediMitraException(ErrorCode errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.details = details == null ? Collections.emptyMap() : Map.copyOf(details);
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Map<String, Object> details() {
        return details;
    }

    public abstract int httpStatus();

    public abstract boolean isRetryable();
}
