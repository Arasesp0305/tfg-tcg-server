package com.tfg.tcgserver.persistence.firebase;

import com.tfg.tcgserver.models.cards.Card;
import com.google.firebase.database.GenericTypeIndicator;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class CardRepository {

    private final FirebaseRealtimeDatabaseRepository firebaseRepository;

    public CardRepository(FirebaseRealtimeDatabaseRepository firebaseRepository) {
        this.firebaseRepository = firebaseRepository;
    }

    public CompletableFuture<Card> findById(String cardId) {
        return firebaseRepository.find(path(cardId), Card.class);
    }

    public CompletableFuture<Map<String, Card>> findAll() {
        GenericTypeIndicator<Map<String, Card>> valueType = new GenericTypeIndicator<>() {
        };

        return firebaseRepository.find("cards", valueType);
    }

    public CompletableFuture<Void> save(String cardId, Card card) {
        return firebaseRepository.save(path(cardId), card);
    }

    public CompletableFuture<Void> delete(String cardId) {
        return firebaseRepository.delete(path(cardId));
    }

    private String path(String cardId) {
        return "cards/" + cardId;
    }
}
