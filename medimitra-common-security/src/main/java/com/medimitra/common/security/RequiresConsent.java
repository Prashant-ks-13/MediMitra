package com.medimitra.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Enforced by {@link ConsentEnforcementAspect}: {@code patientIdParam} names the method parameter holding the patient id to check {@code action} against. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresConsent {
    String action();

    String patientIdParam();
}
