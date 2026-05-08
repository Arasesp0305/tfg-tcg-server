package com.tfg.tcgserver.api.dto;

public record MatchmakingResponse(
        boolean matched,
        String gameId,
        int queueSize
) {
}
