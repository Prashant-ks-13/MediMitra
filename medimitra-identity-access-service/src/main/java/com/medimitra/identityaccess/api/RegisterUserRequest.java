package com.medimitra.identityaccess.api;

import com.medimitra.common.contracts.event.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Email String email, @NotBlank @Size(min = 8) String password, @NotNull UserRole role) {
}
