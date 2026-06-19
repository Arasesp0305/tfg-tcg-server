package com.tfg.tcgserver.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserProfileRequest(
        @NotBlank String username,
        @NotBlank String avatarId
) {
}
