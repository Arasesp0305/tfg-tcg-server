package com.tfg.tcgserver.persistence.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.tfg.tcgserver.models.cards.Card;
import com.tfg.tcgserver.models.cards.CardType;
import com.tfg.tcgserver.models.player.UserProfile;
import com.tfg.tcgserver.models.player.Deck;
import com.tfg.tcgserver.models.player.PurchaseTransaction;
import com.tfg.tcgserver.models.cards.DeckCard;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

@Repository
public class UserProfileRepository {

    private final FirebaseRealtimeDatabaseRepository firebaseRepository;

    public UserProfileRepository(FirebaseRealtimeDatabaseRepository firebaseRepository) {
        this.firebaseRepository = firebaseRepository;
    }

    public CompletableFuture<UserProfile> findById(String uid) {
        return firebaseRepository.find(path(uid), UserProfile.class);
    }

    public CompletableFuture<Void> save(String uid, UserProfile userProfile) {
        return firebaseRepository.save(path(uid), userProfile);
    }

    public CompletableFuture<Void> saveDeck(String uid, String deckId, Deck deck) {
        return firebaseRepository.save(path(uid) + "/decks/" + deckId, deck);
    }

    public CompletableFuture<Void> deleteDeck(String uid, String deckId) {
        return firebaseRepository.delete(path(uid) + "/decks/" + deckId);
    }

    public CompletableFuture<Void> updateSelectedDeck(String uid, String deckId) {
        return firebaseRepository.save(path(uid) + "/selectedDeckId", deckId);
    }

    public CompletableFuture<List<String>> getDeckActionCardIds(String uid, String deckId, Map<String, Card> catalog) {
        return firebaseRepository.find(path(uid) + "/decks/" + deckId, Deck.class)
                .thenApply(deck -> {
                    List<String> result = new ArrayList<>();

                    if (deck == null || deck.getCards() == null) {
                        return result;
                    }

                    for (DeckCard deckCard : deck.getCards().values()) {
                        Card card = catalog.get(deckCard.getCardId());

                        if (card != null && card.getType() == CardType.ACTION) {
                            for (int i = 0; i < deckCard.getQuantity(); i++) {
                                result.add(deckCard.getCardId());
                            }
                        }
                    }

                    return result;
                });
    }

    public CompletableFuture<PurchaseTransaction> registerPurchase(String uid, String transactionId, PurchaseTransaction transaction) {
        return registerPurchase(uid, transactionId, transaction, Map.of());
    }

    public CompletableFuture<PurchaseTransaction> registerPackPurchase(
            String uid,
            String transactionId,
            PurchaseTransaction transaction,
            Map<String, Integer> cardRewards
    ) {
        return registerPurchase(uid, transactionId, transaction, cardRewards);
    }

    private CompletableFuture<PurchaseTransaction> registerPurchase(
            String uid,
            String transactionId,
            PurchaseTransaction transaction,
            Map<String, Integer> cardRewards
    ) {
        // La referencia aun no esta sincronizada localmente la primera vez que se usa:
        // runTransaction invocaria doTransaction con currentData null aunque el perfil
        // exista en el servidor. Forzamos una lectura previa para poblar la cache local
        // antes de iniciar la transaccion (y devolver un error claro si de verdad no existe).
        return findById(uid).thenCompose(profile -> {
            if (profile == null) {
                CompletableFuture<PurchaseTransaction> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IllegalArgumentException("No existe el perfil del usuario"));
                return failed;
            }

            return runPurchaseTransaction(uid, transactionId, transaction, cardRewards);
        });
    }

    private CompletableFuture<PurchaseTransaction> runPurchaseTransaction(
            String uid,
            String transactionId,
            PurchaseTransaction transaction,
            Map<String, Integer> cardRewards
    ) {
        CompletableFuture<PurchaseTransaction> future = new CompletableFuture<>();
        AtomicReference<String> errorMessage = new AtomicReference<>();

        firebaseRepository.reference(path(uid)).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    errorMessage.set("No existe el perfil del usuario");
                    return Transaction.abort();
                }

                Long currentCoins = currentData.child("coins").getValue(Long.class);

                if (currentCoins == null) {
                    errorMessage.set("El usuario no tiene saldo de monedas");
                    return Transaction.abort();
                }

                if (currentCoins.intValue() < transaction.getCoinAmount()) {
                    errorMessage.set("No tienes monedas suficientes para realizar esta compra");
                    return Transaction.abort();
                }

                int balanceBefore = currentCoins.intValue();
                int balanceAfter = balanceBefore - transaction.getCoinAmount();
                transaction.setBalanceBefore(balanceBefore);
                transaction.setBalanceAfter(balanceAfter);

                currentData.child("coins").setValue(balanceAfter);
                currentData.child("updatedAt").setValue(transaction.getCreatedAt());
                currentData.child("purchaseHistory").child(transactionId).setValue(transaction);
                addCardRewards(currentData, cardRewards, transaction.getCreatedAt());

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    future.completeExceptionally(error.toException());
                    return;
                }

                if (!committed) {
                    String message = errorMessage.get() != null
                            ? errorMessage.get()
                            : "No se pudo registrar la compra";
                    future.completeExceptionally(new IllegalStateException(message));
                    return;
                }

                future.complete(transaction);
            }
        });

        return future;
    }

    private void addCardRewards(MutableData currentData, Map<String, Integer> cardRewards, String updatedAt) {
        for (Map.Entry<String, Integer> entry : cardRewards.entrySet()) {
            String cardId = entry.getKey();
            int rewardQuantity = entry.getValue();
            MutableData ownedCard = currentData.child("cardCollection").child(cardId);
            Long currentQuantity = ownedCard.child("quantity").getValue(Long.class);
            Object firstObtainedAt = ownedCard.child("firstObtainedAt").getValue();

            ownedCard.child("cardId").setValue(cardId);
            ownedCard.child("quantity").setValue((currentQuantity == null ? 0 : currentQuantity.intValue()) + rewardQuantity);
            ownedCard.child("updatedAt").setValue(updatedAt);

            if (firstObtainedAt == null) {
                ownedCard.child("firstObtainedAt").setValue(updatedAt);
            }
        }
    }

    private String path(String uid) {
        return "users/" + uid;
    }
}
