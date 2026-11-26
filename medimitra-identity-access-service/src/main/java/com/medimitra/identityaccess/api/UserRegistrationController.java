package com.medimitra.identityaccess.api;

import com.medimitra.common.core.Region;
import com.medimitra.common.security.MediMitraSecurityProperties;
import com.medimitra.identityaccess.application.UserRegistrationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** {@code /api/v1/auth/register} — public (the gateway marks the whole {@code /api/v1/auth/**} prefix unauthenticated). */
@RestController
@RequestMapping("/api/v1/auth")
public class UserRegistrationController {

    private final UserRegistrationService service;
    private final Region region;

    public UserRegistrationController(UserRegistrationService service, MediMitraSecurityProperties securityProperties) {
        this.service = service;
        this.region = Region.valueOf(securityProperties.getRegion());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        UUID userId = service.register(request.email(), request.password(), request.role(), region);
        return new RegisterUserResponse(userId);
    }
}
