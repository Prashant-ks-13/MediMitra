package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

public class ConsentRequiredException extends SecurityViolationException {

    public ConsentRequiredException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public ConsentRequiredException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }

    @Override
    public int httpStatus() {
        return 403;
    }
}
