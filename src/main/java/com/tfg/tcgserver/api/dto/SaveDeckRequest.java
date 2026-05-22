package com.tfg.tcgserver.api.dto;

import com.tfg.tcgserver.models.cards.DeckCard;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record SaveDeckRequest(
        @NotBlank String name,
        @NotEmpty Map<String, DeckCard> cards
) {
}
