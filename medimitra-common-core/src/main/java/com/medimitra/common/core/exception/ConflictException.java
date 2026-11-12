package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

public class ConflictException extends BusinessException {

    public ConflictException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of(), null);
    }

    public ConflictException(ErrorCode errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(errorCode, message, details, cause);
    }

    @Override
    public int httpStatus() {
        return 409;
    }
}
