package com.medimitra.identityaccess.config;

import com.medimitra.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * {@code medimitra-common-security}'s {@code JwtAuthenticationFilter} is not wired into any
 * filter chain automatically (by design) — each service adds it to its own chain, here.
 *
 * <p>Only the endpoints that are how a caller gets a token in the first place stay public
 * (register/login/OTP/refresh). {@code /auth/logout} and {@code /auth/step-up} need the caller's
 * current token (to read its {@code jti}/{@code sid} for denylisting, or to prove an active
 * session before stepping up) and are deliberately left out of this list, enforced here even
 * though the gateway's own edge filter also marks the whole {@code /api/v1/auth/**} prefix public
 * for fail-fast routing — this service's own check is the real, zero-trust enforcement point.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/otp/**", "/api/v1/auth/refresh",
                                "/actuator/health")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
