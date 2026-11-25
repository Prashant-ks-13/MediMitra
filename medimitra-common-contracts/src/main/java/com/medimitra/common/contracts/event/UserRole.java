package com.medimitra.common.contracts.event;

/** A role granted at sign-up; carried on {@link UserRegisteredV1} and mirrored in JWT claims. */
public enum UserRole {
    PATIENT,
    CAREGIVER,
    DOCTOR,
    LAB_STAFF,
    ADMIN
}
