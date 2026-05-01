package com.tfg.tcgserver.service.matchmaking;

import com.tfg.tcgserver.matchmaking.MatchmakingEntry;
import com.tfg.tcgserver.matchmaking.MatchmakingQueue;
import com.tfg.tcgserver.service.game.GameService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class MatchmakingService {

    private final MatchmakingQueue matchmakingQueue;
    private final GameService gameService;

    public MatchmakingService(MatchmakingQueue matchmakingQueue, GameService gameService) {
        this.matchmakingQueue = matchmakingQueue;
        this.gameService = gameService;
    }

    public CompletableFuture<MatchmakingResult> findMatch(String playerId, String deckId, int mmr) {
        Optional<String> existingMatch = matchmakingQueue.consumeMatchedGame(playerId);
        if (existingMatch.isPresent()) {
            return CompletableFuture.completedFuture(MatchmakingResult.matched(existingMatch.get()));
        }

        MatchmakingEntry player = new MatchmakingEntry(playerId, deckId, mmr, Instant.now().toString());
        Optional<MatchmakingEntry> opponent = matchmakingQueue.findOpponentFor(player);

        if (opponent.isEmpty()) {
            return CompletableFuture.completedFuture(MatchmakingResult.waiting(matchmakingQueue.size()));
        }

        MatchmakingEntry matchedOpponent = opponent.get();

        return gameService.createGame(
                        matchedOpponent.getPlayerId(),
                        matchedOpponent.getDeckId(),
                        player.getPlayerId(),
                        player.getDeckId()
                )
                .thenApply(gameId -> {
                    matchmakingQueue.storeMatchedGame(matchedOpponent.getPlayerId(), gameId);
                    return MatchmakingResult.matched(gameId);
                });
    }
}
