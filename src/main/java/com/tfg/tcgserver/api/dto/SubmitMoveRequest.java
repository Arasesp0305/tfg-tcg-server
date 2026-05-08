package com.tfg.tcgserver.api.dto;

import com.tfg.tcgserver.models.game.MoveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitMoveRequest(
        @NotBlank String playerId,
        @NotNull MoveType type,
        String cardId,
        String sourceFieldCardId,
        String targetFieldCardId
) {
}
