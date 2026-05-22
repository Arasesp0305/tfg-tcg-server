package com.tfg.tcgserver.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SelectedActionRequest(
        @NotBlank String sourceFieldCardId,
        @NotBlank String actionCardId,
        @NotBlank String targetFieldCardId
) {
}
