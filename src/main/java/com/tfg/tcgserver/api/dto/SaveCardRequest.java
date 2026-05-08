package com.tfg.tcgserver.api.dto;

import com.tfg.tcgserver.models.cards.CardEffect;
import com.tfg.tcgserver.models.cards.CardStats;
import com.tfg.tcgserver.models.cards.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveCardRequest(
        @NotBlank String name,
        @NotNull CardType type,
        String image,
        String text,
        CardStats stats,
        CardEffect effect
) {
}
