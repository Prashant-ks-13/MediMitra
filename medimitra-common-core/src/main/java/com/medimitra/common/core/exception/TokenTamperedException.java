package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

import java.util.Map;

/** Signature, algorithm allowlist, DPoP proof or claim check failed on an access token. */
public class TokenTamperedException extends SecurityViolationException {

    public TokenTamperedException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public TokenTamperedException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details, null);
    }

    @Override
    public int httpStatus() {
        return 401;
    }
}
