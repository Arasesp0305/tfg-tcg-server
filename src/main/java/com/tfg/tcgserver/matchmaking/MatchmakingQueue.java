package com.tfg.tcgserver.matchmaking;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

@Component
public class MatchmakingQueue {

    private final Queue<MatchmakingEntry> queue = new ArrayDeque<>();
    private final Map<String, String> matchedGamesByPlayerId = new HashMap<>();

    public synchronized Optional<MatchmakingEntry> findOpponentFor(MatchmakingEntry player) {
        MatchmakingEntry opponent = queue.poll();

        if (opponent == null) {
            queue.offer(player);
            return Optional.empty();
        }

        if (opponent.getPlayerId().equals(player.getPlayerId())) {
            queue.offer(opponent);
            return Optional.empty();
        }

        return Optional.of(opponent);
    }

    public synchronized Optional<String> consumeMatchedGame(String playerId) {
        return Optional.ofNullable(matchedGamesByPlayerId.remove(playerId));
    }

    public synchronized void storeMatchedGame(String playerId, String gameId) {
        matchedGamesByPlayerId.put(playerId, gameId);
    }

    public synchronized int size() {
        return queue.size();
    }
}
