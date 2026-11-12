package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

/** The token's region claim does not match this cell's region. Raised by {@code RegionGuard}. */
public class RegionMismatchException extends SecurityViolationException {

    public RegionMismatchException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public RegionMismatchException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }

    @Override
    public int httpStatus() {
        return 403;
    }
}
