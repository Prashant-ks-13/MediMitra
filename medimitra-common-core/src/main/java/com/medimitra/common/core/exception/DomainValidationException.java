package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

/** A domain invariant checked inside an aggregate method failed, as opposed to simple Bean Validation on a DTO. */
public class DomainValidationException extends BusinessException {

    public DomainValidationException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public DomainValidationException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }

    @Override
    public int httpStatus() {
        return 400;
    }
}
