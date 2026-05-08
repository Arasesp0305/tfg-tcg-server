package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.api.dto.FindMatchRequest;
import com.tfg.tcgserver.api.dto.MatchmakingResponse;
import com.tfg.tcgserver.service.matchmaking.MatchmakingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController extends AuthenticatedController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/find")
    public CompletableFuture<MatchmakingResponse> findMatch(
            @Valid @RequestBody FindMatchRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(request.playerId(), servletRequest);

        return matchmakingService.findMatch(request.playerId(), request.deckId(), request.mmr())
                .thenApply(result -> new MatchmakingResponse(
                        result.isMatched(),
                        result.getGameId(),
                        result.getQueueSize()
                ));
    }
}
