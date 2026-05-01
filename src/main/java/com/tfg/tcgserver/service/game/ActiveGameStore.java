package com.tfg.tcgserver.service.game;

import com.tfg.tcgserver.models.game.Game;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveGameStore {

    private final Map<String, Game> activeGames = new ConcurrentHashMap<>();

    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(activeGames.get(gameId));
    }

    public void save(String gameId, Game game) {
        activeGames.put(gameId, game);
    }

    public Optional<Game> remove(String gameId) {
        return Optional.ofNullable(activeGames.remove(gameId));
    }
}
