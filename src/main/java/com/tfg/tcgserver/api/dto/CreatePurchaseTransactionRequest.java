package com.tfg.tcgserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreatePurchaseTransactionRequest(
        @NotBlank String itemId,
        @NotBlank String itemName,
        @Positive int coinAmount,
        String description
) {
}
