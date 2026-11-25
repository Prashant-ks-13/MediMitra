package com.medimitra.common.web;

import com.medimitra.common.core.error.ErrorCode;

/** Stable codes raised by this module, all under the cross-cutting {@code WEB} service segment. */
public final class WebErrorCodes {

    private WebErrorCodes() {
    }

    public static final ErrorCode IDEMPOTENCY_KEY_MISSING = new ErrorCode("MM-WEB-400-IDEMPOTENCY_KEY_MISSING");
    public static final ErrorCode IDEMPOTENCY_KEY_REUSED = new ErrorCode("MM-WEB-422-IDEMPOTENCY_KEY_REUSED");
    public static final ErrorCode VALIDATION_FAILED = new ErrorCode("MM-WEB-400-VALIDATION_FAILED");
    public static final ErrorCode INTERNAL_ERROR = new ErrorCode("MM-WEB-500-INTERNAL_ERROR");
}
