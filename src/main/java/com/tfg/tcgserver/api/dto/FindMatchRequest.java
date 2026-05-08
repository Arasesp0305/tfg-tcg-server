package com.tfg.tcgserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record FindMatchRequest(
        @NotBlank String playerId,
        @NotBlank String deckId,
        @PositiveOrZero int mmr
) {
}
