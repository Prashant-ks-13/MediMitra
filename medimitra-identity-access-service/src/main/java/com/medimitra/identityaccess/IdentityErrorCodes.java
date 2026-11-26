package com.medimitra.identityaccess;

import com.medimitra.common.core.error.ErrorCode;

/** Stable codes raised by this service, under the {@code IAM} service segment. */
public final class IdentityErrorCodes {

    private IdentityErrorCodes() {
    }

    public static final ErrorCode EMAIL_ALREADY_REGISTERED = new ErrorCode("MM-IAM-409-EMAIL_ALREADY_REGISTERED");
    public static final ErrorCode LOGIN_FAILED = new ErrorCode("MM-IAM-401-LOGIN_FAILED");
    public static final ErrorCode OTP_INVALID_OR_EXPIRED = new ErrorCode("MM-IAM-401-OTP_INVALID_OR_EXPIRED");
    public static final ErrorCode REFRESH_FAILED = new ErrorCode("MM-IAM-401-REFRESH_FAILED");
    public static final ErrorCode LOGOUT_FAILED = new ErrorCode("MM-IAM-503-LOGOUT_FAILED");
    public static final ErrorCode ACCOUNT_NOT_FOUND = new ErrorCode("MM-IAM-404-ACCOUNT_NOT_FOUND");
}
