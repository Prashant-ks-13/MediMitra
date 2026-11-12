package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

/** A violation of a domain rule; never retryable by simply resubmitting the same request. */
public abstract class BusinessException extends MediMitraException {

    protected BusinessException(ErrorCode errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(errorCode, message, details, cause);
    }

    @Override
    public boolean isRetryable() {
        return false;
    }
}
