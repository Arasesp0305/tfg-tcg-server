package com.tfg.tcgserver.persistence.firebase;

import com.google.firebase.database.GenericTypeIndicator;
import com.tfg.tcgserver.models.game.Game;
import com.tfg.tcgserver.models.game.Move;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class GameRepository {

    private final FirebaseRealtimeDatabaseRepository firebaseRepository;

    public GameRepository(FirebaseRealtimeDatabaseRepository firebaseRepository) {
        this.firebaseRepository = firebaseRepository;
    }

    public CompletableFuture<Game> findById(String gameId) {
        return firebaseRepository.find(path(gameId), Game.class);
    }

    public CompletableFuture<Map<String, Game>> findAll() {
        GenericTypeIndicator<Map<String, Game>> valueType = new GenericTypeIndicator<>() {
        };

        return firebaseRepository.find("games", valueType);
    }

    public CompletableFuture<Void> save(String gameId, Game game) {
        return firebaseRepository.save(path(gameId), game);
    }

    public CompletableFuture<Void> saveMove(String gameId, String turnId, String moveId, Move move) {
        return firebaseRepository.save(path(gameId) + "/turns/" + turnId + "/moves/" + moveId, move);
    }

    private String path(String gameId) {
        return "games/" + gameId;
    }
}
