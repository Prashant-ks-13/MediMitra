package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

/** An aggregate rejected a transition its own state machine does not allow. */
public class InvalidStateTransitionException extends BusinessException {

    public InvalidStateTransitionException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public InvalidStateTransitionException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }

    @Override
    public int httpStatus() {
        return 409;
    }
}
