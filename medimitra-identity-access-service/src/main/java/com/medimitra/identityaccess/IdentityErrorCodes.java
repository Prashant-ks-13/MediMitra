package com.medimitra.identityaccess;

import com.medimitra.common.core.error.ErrorCode;

/** Stable codes raised by this service, under the {@code IAM} service segment. */
public final class IdentityErrorCodes {

    private IdentityErrorCodes() {
    }

    public static final ErrorCode EMAIL_ALREADY_REGISTERED = new ErrorCode("MM-IAM-409-EMAIL_ALREADY_REGISTERED");
}
