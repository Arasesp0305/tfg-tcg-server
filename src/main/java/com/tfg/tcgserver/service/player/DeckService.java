package com.tfg.tcgserver.service.player;

import com.tfg.tcgserver.models.cards.Card;
import com.tfg.tcgserver.models.cards.DeckCard;
import com.tfg.tcgserver.models.player.Deck;
import com.tfg.tcgserver.models.player.UserOwnedCard;
import com.tfg.tcgserver.models.player.UserProfile;
import com.tfg.tcgserver.models.rules.DeckValidator;
import com.tfg.tcgserver.persistence.firebase.CardRepository;
import com.tfg.tcgserver.persistence.firebase.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DeckService {

    private final UserProfileRepository userProfileRepository;
    private final CardRepository cardRepository;
    private final DeckValidator deckValidator;

    public DeckService(
            UserProfileRepository userProfileRepository,
            CardRepository cardRepository,
            DeckValidator deckValidator
    ) {
        this.userProfileRepository = userProfileRepository;
        this.cardRepository = cardRepository;
        this.deckValidator = deckValidator;
    }

    public CompletableFuture<Map<String, Deck>> getDecks(String uid) {
        return userProfileRepository.findById(uid)
                .thenApply(profile -> profile == null ? Map.of() : profile.getDecks());
    }

    public CompletableFuture<Deck> getDeck(String uid, String deckId) {
        return userProfileRepository.findById(uid)
                .thenApply(profile -> {
                    if (profile == null || profile.getDecks() == null || !profile.getDecks().containsKey(deckId)) {
                        throw new IllegalArgumentException("El mazo no existe");
                    }

                    return profile.getDecks().get(deckId);
                });
    }

    public CompletableFuture<String> createDeck(String uid, Deck deck) {
        String deckId = "deck_" + UUID.randomUUID();
        return saveDeck(uid, deckId, deck)
                .thenApply(savedDeck -> deckId);
    }

    public CompletableFuture<Deck> saveDeck(String uid, String deckId, Deck deck) {
        String now = Instant.now().toString();

        return userProfileRepository.findById(uid)
                .thenCompose(profile -> {
                    if (profile == null) {
                        throw new IllegalArgumentException("No existe el perfil del usuario");
                    }

                    if (deck.getCreatedAt() == null) {
                        Deck existingDeck = profile.getDecks() == null ? null : profile.getDecks().get(deckId);
                        deck.setCreatedAt(existingDeck == null ? now : existingDeck.getCreatedAt());
                    }

                    deck.setUpdatedAt(now);

                    validateOwnedCards(deck, profile);

                    return cardRepository.findAll()
                            .thenCompose(cardCatalog -> {
                                Map<String, Card> safeCatalog = cardCatalog == null ? Map.of() : cardCatalog;
                                deck.setCardCount(deckValidator.calculateCardCount(deck, safeCatalog));
                                deck.setPlayable(deckValidator.isValid(deck, safeCatalog));

                                return userProfileRepository.saveDeck(uid, deckId, deck);
                            });
                })
                .thenApply(ignored -> deck);
    }

    private void validateOwnedCards(Deck deck, UserProfile profile) {
        Map<String, UserOwnedCard> collection = profile.getCardCollection() == null
                ? Map.of()
                : profile.getCardCollection();

        if (deck.getCards() == null || deck.getCards().isEmpty()) {
            throw new IllegalArgumentException("El mazo debe tener cartas");
        }

        for (DeckCard deckCard : deck.getCards().values()) {
            if (deckCard.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada carta debe ser mayor que 0");
            }

            UserOwnedCard ownedCard = collection.get(deckCard.getCardId());

            if (ownedCard == null || ownedCard.getQuantity() < deckCard.getQuantity()) {
                throw new IllegalArgumentException("No tienes suficientes copias de la carta " + deckCard.getCardId());
            }
        }
    }

    public CompletableFuture<Void> deleteDeck(String uid, String deckId) {
        return userProfileRepository.deleteDeck(uid, deckId);
    }

    public CompletableFuture<Void> selectDeck(String uid, String deckId) {
        return getDeck(uid, deckId)
                .thenCompose(deck -> {
                    if (!deck.isPlayable()) {
                        throw new IllegalArgumentException("No puedes seleccionar un mazo marcado como not playable");
                    }

                    return userProfileRepository.updateSelectedDeck(uid, deckId);
                });
    }
}
