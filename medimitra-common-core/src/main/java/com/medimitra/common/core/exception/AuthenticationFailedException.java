package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

public class AuthenticationFailedException extends SecurityViolationException {

    public AuthenticationFailedException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public AuthenticationFailedException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }

    @Override
    public int httpStatus() {
        return 401;
    }
}
