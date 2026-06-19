package com.tfg.tcgserver.api.dto;

import com.tfg.tcgserver.models.game.GameStatus;

public record GameSummaryResponse(
        String gameId,
        GameStatus status,
        String createdAt,
        String startedAt,
        String finishedAt,
        String player1Id,
        String player2Id,
        String winnerId,
        String opponentId
) {
}
