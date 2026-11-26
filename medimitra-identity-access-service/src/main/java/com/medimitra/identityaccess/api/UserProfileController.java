package com.medimitra.identityaccess.api;

import com.medimitra.common.security.AuthenticatedPrincipal;
import com.medimitra.identityaccess.application.UserProfileService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code /api/v1/users/**} — requires a valid token (not in {@link com.medimitra.identityaccess.config.SecurityConfig}'s permitAll list). */
@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UserProfileResponse me() {
        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication();
        return UserProfileResponse.from(service.getProfile(principal.subject().value()));
    }
}
