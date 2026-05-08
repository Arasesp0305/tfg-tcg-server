package com.tfg.tcgserver.service.cards;

import com.tfg.tcgserver.models.cards.Card;
import com.tfg.tcgserver.persistence.firebase.CardRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CompletableFuture<Map<String, Card>> getCards() {
        return cardRepository.findAll();
    }

    public CompletableFuture<Card> getCard(String cardId) {
        return cardRepository.findById(cardId);
    }

    public CompletableFuture<Card> saveCard(String cardId, Card card) {
        return cardRepository.save(cardId, card)
                .thenApply(ignored -> card);
    }

    public CompletableFuture<Void> deleteCard(String cardId) {
        return cardRepository.delete(cardId);
    }
}
