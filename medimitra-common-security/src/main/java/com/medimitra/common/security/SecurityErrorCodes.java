package com.medimitra.common.security;

import com.medimitra.common.core.error.ErrorCode;

/** Stable codes raised by this module, all under the cross-cutting {@code SEC} service segment. */
public final class SecurityErrorCodes {

    private SecurityErrorCodes() {
    }

    public static final ErrorCode INVALID_ALGORITHM = new ErrorCode("MM-SEC-401-INVALID_ALGORITHM");
    public static final ErrorCode UNKNOWN_KEY_ID = new ErrorCode("MM-SEC-401-UNKNOWN_KEY_ID");
    public static final ErrorCode SIGNATURE_INVALID = new ErrorCode("MM-SEC-401-SIGNATURE_INVALID");
    public static final ErrorCode ISSUER_MISMATCH = new ErrorCode("MM-SEC-401-ISSUER_MISMATCH");
    public static final ErrorCode AUDIENCE_MISMATCH = new ErrorCode("MM-SEC-401-AUDIENCE_MISMATCH");
    public static final ErrorCode TOKEN_TYPE_INVALID = new ErrorCode("MM-SEC-401-TOKEN_TYPE_INVALID");
    public static final ErrorCode TOKEN_EXPIRED = new ErrorCode("MM-SEC-401-TOKEN_EXPIRED");
    public static final ErrorCode TOKEN_NOT_YET_VALID = new ErrorCode("MM-SEC-401-TOKEN_NOT_YET_VALID");
    public static final ErrorCode TOKEN_REVOKED = new ErrorCode("MM-SEC-401-TOKEN_REVOKED");
    public static final ErrorCode DPOP_PROOF_MISSING = new ErrorCode("MM-SEC-401-DPOP_PROOF_MISSING");
    public static final ErrorCode DPOP_PROOF_INVALID = new ErrorCode("MM-SEC-401-DPOP_PROOF_INVALID");
    public static final ErrorCode DPOP_PROOF_REPLAYED = new ErrorCode("MM-SEC-401-DPOP_PROOF_REPLAYED");
    public static final ErrorCode REGION_MISMATCH = new ErrorCode("MM-SEC-403-REGION_MISMATCH");
    public static final ErrorCode STEP_UP_REQUIRED = new ErrorCode("MM-SEC-401-STEP_UP_REQUIRED");
    public static final ErrorCode CONSENT_REQUIRED = new ErrorCode("MM-SEC-403-CONSENT_REQUIRED");
}
