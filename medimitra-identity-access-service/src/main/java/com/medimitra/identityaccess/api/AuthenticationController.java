package com.medimitra.identityaccess.api;

import com.medimitra.identityaccess.application.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** {@code /api/v1/auth/**} — public (the gateway marks this whole prefix unauthenticated). */
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
}
