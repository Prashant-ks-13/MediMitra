package com.medimitra.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Opt-in switch for {@code GlobalExceptionHandler}, the correlation-id filter, PII-free request
 * logging and idempotency. No bean in this module is registered unless a service declares this,
 * per the common-module rule against hidden beans.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MediMitraWebAutoConfiguration.class)
public @interface EnableMediMitraWeb {
}
