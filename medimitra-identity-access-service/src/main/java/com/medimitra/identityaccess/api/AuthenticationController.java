package com.medimitra.identityaccess.api;

import com.medimitra.common.security.AuthenticatedPrincipal;
import com.medimitra.identityaccess.application.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code /api/v1/auth/**}. Register/login/OTP/refresh are public (how a caller gets a token at
 * all); logout and step-up require the caller's current token, per {@link com.medimitra.identityaccess.config.SecurityConfig}.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public TokenResponseDto login(@Valid @RequestBody LoginRequest request, @RequestHeader(name = "DPoP", required = false) String dpopProof) {
        return TokenResponseDto.from(service.login(request.email(), request.password(), dpopProof));
    }

    @PostMapping("/otp/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestOtp(@Valid @RequestBody OtpRequestRequest request) {
        service.requestOtp(request.email());
    }

    @PostMapping("/otp/verify")
    public TokenResponseDto verifyOtp(@Valid @RequestBody OtpVerifyRequest request, @RequestHeader(name = "DPoP", required = false) String dpopProof) {
        return TokenResponseDto.from(service.verifyOtp(request.email(), request.code(), dpopProof));
    }

    @PostMapping("/refresh")
    public TokenResponseDto refresh(@Valid @RequestBody RefreshRequest request, @RequestHeader(name = "DPoP", required = false) String dpopProof) {
        return TokenResponseDto.from(service.refresh(request.refreshToken(), dpopProof));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        AuthenticatedPrincipal principal = currentPrincipal();
        service.logout(request.refreshToken(), principal.jti(), principal.sid());
    }

    @PostMapping("/step-up")
    public TokenResponseDto stepUp(@Valid @RequestBody OtpVerifyRequest request, @RequestHeader(name = "DPoP", required = false) String dpopProof) {
        currentPrincipal(); // requires an existing valid session; the fresh token below supersedes it
        return TokenResponseDto.from(service.stepUp(request.email(), request.code(), dpopProof));
    }

    private static AuthenticatedPrincipal currentPrincipal() {
        return (AuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication();
    }
}
