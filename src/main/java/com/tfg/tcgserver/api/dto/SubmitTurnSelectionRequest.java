package com.tfg.tcgserver.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SubmitTurnSelectionRequest(
        @NotBlank String playerId,
        @Valid @Size(max = 2) List<SelectedActionRequest> actions
) {
}
