package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

public class SlotUnavailableException extends ConflictException {

    public SlotUnavailableException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SlotUnavailableException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }
}
