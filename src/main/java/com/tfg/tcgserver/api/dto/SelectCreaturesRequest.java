package com.tfg.tcgserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SelectCreaturesRequest(
        @NotBlank String playerId,
        @Size(min = 2, max = 2) List<String> activeCreatureCardIds,
        @NotBlank String hiddenCreatureCardId
) {
}
