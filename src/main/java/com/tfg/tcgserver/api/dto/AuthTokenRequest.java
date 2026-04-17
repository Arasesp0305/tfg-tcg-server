package com.tfg.tcgserver.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthTokenRequest(
        @NotBlank String idToken
) {
}
