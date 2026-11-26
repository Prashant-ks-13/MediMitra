package com.medimitra.identityaccess.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpRequestRequest(@NotBlank @Email String email) {
}
