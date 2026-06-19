package com.tfg.tcgserver.matchmaking;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class MatchmakingQueue {

    private final Queue<MatchmakingEntry> queue = new ArrayDeque<>();
    private final Map<String, String> matchedGamesByPlayerId = new HashMap<>();
    private final Map<String, CompletableFuture<String>> matchWaiters = new HashMap<>();

    public synchronized Optional<MatchmakingEntry> findOpponentFor(MatchmakingEntry player) {
        MatchmakingEntry opponent = queue.poll();

        if (opponent == null) {
            queue.offer(player);
            return Optional.empty();
        }

        if (opponent.getPlayerId().equals(player.getPlayerId())) {
            // Entrada propia de un long poll anterior (timeout sin rival): se sustituye por la nueva
            queue.offer(player);
            return Optional.empty();
        }

        return Optional.of(opponent);
    }

    public synchronized Optional<String> consumeMatchedGame(String playerId) {
        return Optional.ofNullable(matchedGamesByPlayerId.remove(playerId));
    }

    public synchronized void storeMatchedGame(String playerId, String gameId) {
        CompletableFuture<String> waiter = matchWaiters.remove(playerId);

        if (waiter != null) {
            waiter.complete(gameId);
        } else {
            matchedGamesByPlayerId.put(playerId, gameId);
        }
    }

    public synchronized int size() {
        return queue.size();
    }

    /**
     * Espera (long polling) hasta que se encuentre rival para {@code playerId}, o hasta el
     * timeout, en cuyo caso se completa con {@code null}.
     */
    public CompletableFuture<String> awaitMatch(String playerId, long timeoutMs) {
        CompletableFuture<String> waiter;

        synchronized (this) {
            String existing = matchedGamesByPlayerId.remove(playerId);
            if (existing != null) {
                return CompletableFuture.completedFuture(existing);
            }

            waiter = new CompletableFuture<>();
            matchWaiters.put(playerId, waiter);
        }

        return waiter.completeOnTimeout(null, timeoutMs, TimeUnit.MILLISECONDS)
                .whenComplete((result, error) -> {
                    synchronized (this) {
                        matchWaiters.remove(playerId);
                    }
                });
    }
}
