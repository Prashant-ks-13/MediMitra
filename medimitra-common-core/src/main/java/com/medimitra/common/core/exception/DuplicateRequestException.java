package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

public class DuplicateRequestException extends ConflictException {

    public DuplicateRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public DuplicateRequestException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }
}
