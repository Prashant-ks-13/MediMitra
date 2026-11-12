package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

/** The same Idempotency-Key was reused with a different request body. */
public class IdempotencyKeyReuseException extends ConflictException {

    public IdempotencyKeyReuseException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public int httpStatus() {
        return 422;
    }
}
