package com.tfg.tcgserver.service.game;

import com.tfg.tcgserver.models.game.Game;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Component
public class ActiveGameStore {

    private static final long REMOVED_VERSION = -1L;

    private final Map<String, Entry> activeGames = new ConcurrentHashMap<>();

    public Optional<Game> findById(String gameId) {
        Entry entry = activeGames.get(gameId);
        return entry == null ? Optional.empty() : Optional.of(entry.game);
    }

    public void save(String gameId, Game game) {
        Entry entry = activeGames.computeIfAbsent(gameId, id -> new Entry());
        entry.version++;
        game.setVersion(entry.version);
        entry.game = game;
        notifyWaiters(entry);
    }

    public Optional<Game> remove(String gameId) {
        Entry entry = activeGames.remove(gameId);
        if (entry == null) {
            return Optional.empty();
        }

        entry.version = REMOVED_VERSION;
        notifyWaiters(entry);
        return Optional.of(entry.game);
    }

    /**
     * Se resuelve en cuanto la version de la partida cambie respecto a {@code sinceVersion},
     * o tras {@code timeoutMs} con la version actual (long polling).
     */
    public CompletableFuture<Long> awaitVersionChange(String gameId, long sinceVersion, long timeoutMs) {
        Entry entry = activeGames.get(gameId);

        if (entry == null) {
            return CompletableFuture.completedFuture(REMOVED_VERSION);
        }

        if (entry.version != sinceVersion) {
            return CompletableFuture.completedFuture(entry.version);
        }

        CompletableFuture<Long> waiter = new CompletableFuture<>();
        entry.waiters.add(waiter);
        return waiter.completeOnTimeout(entry.version, timeoutMs, TimeUnit.MILLISECONDS);
    }

    private void notifyWaiters(Entry entry) {
        List<CompletableFuture<Long>> pending = new ArrayList<>(entry.waiters);
        entry.waiters.clear();

        for (CompletableFuture<Long> waiter : pending) {
            waiter.complete(entry.version);
        }
    }

    private static class Entry {
        private Game game;
        private long version;
        private final List<CompletableFuture<Long>> waiters = new CopyOnWriteArrayList<>();
    }
}
