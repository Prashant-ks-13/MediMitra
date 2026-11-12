package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

/** Authentication, token or access-control failure. Never retryable without the caller changing something. */
public abstract class SecurityViolationException extends MediMitraException {

    protected SecurityViolationException(ErrorCode errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(errorCode, message, details, cause);
    }

    @Override
    public boolean isRetryable() {
        return false;
    }
}
