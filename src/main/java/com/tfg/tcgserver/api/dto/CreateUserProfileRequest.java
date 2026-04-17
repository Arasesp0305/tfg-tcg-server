package com.tfg.tcgserver.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserProfileRequest(
        @NotBlank String uid,
        @NotBlank String username,
        @Email @NotBlank String email
) {
}
