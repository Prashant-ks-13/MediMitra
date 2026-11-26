package com.medimitra.patientprofile;

import com.medimitra.common.core.error.ErrorCode;

/** Stable codes raised by this service, under the {@code PAT} service segment. */
public final class PatientErrorCodes {

    private PatientErrorCodes() {
    }

    public static final ErrorCode PATIENT_NOT_FOUND = new ErrorCode("MM-PAT-404-PATIENT_NOT_FOUND");
    /** Thrown when a caller who is not the patient themselves tries to read/write the record (section 3.1 zero trust). */
    public static final ErrorCode CONSENT_REQUIRED = new ErrorCode("MM-PAT-403-CONSENT_REQUIRED");
}
