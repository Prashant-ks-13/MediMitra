package com.medimitra.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * Opt-in switch for JWT/DPoP validation, region guarding, service tokens and consent enforcement.
 * No bean in this module is registered unless a service declares this, per the common-module rule
 * against hidden beans. The produced {@link JwtAuthenticationFilter} bean is not wired into any
 * filter chain automatically — each service adds it to its own {@code SecurityFilterChain}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableAspectJAutoProxy
@Import(MediMitraSecurityAutoConfiguration.class)
public @interface EnableMediMitraSecurity {
}
