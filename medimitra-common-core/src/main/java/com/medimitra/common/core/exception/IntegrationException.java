package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

/** A failure calling an external or downstream system, as opposed to a domain rule violation. */
public abstract class IntegrationException extends MediMitraException {

    protected IntegrationException(ErrorCode errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(errorCode, message, details, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
