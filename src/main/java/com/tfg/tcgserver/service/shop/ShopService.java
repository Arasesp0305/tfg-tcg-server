package com.tfg.tcgserver.service.shop;

import com.tfg.tcgserver.models.player.PurchaseTransaction;
import com.tfg.tcgserver.models.player.PurchaseTransactionType;
import com.tfg.tcgserver.models.shop.OpenPackResult;
import com.tfg.tcgserver.models.shop.PackProduct;
import com.tfg.tcgserver.persistence.firebase.CardRepository;
import com.tfg.tcgserver.persistence.firebase.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ShopService {

    private final CardRepository cardRepository;
    private final UserProfileRepository userProfileRepository;
    private final Random random = new Random();

    public ShopService(CardRepository cardRepository, UserProfileRepository userProfileRepository) {
        this.cardRepository = cardRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public CompletableFuture<List<PackProduct>> getPacks() {
        return ensureSampleCards()
                .thenApply(ignored -> SampleCatalog.packs());
    }

    public CompletableFuture<OpenPackResult> openPack(String uid, String packId) {
        PackProduct pack = findPack(packId);
        Map<String, Integer> rewards = drawRewards(pack);

        String transactionId = "purchase_" + UUID.randomUUID();
        String now = Instant.now().toString();

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(transactionId);
        transaction.setItemId(pack.getId());
        transaction.setItemName(pack.getName());
        transaction.setType(PurchaseTransactionType.PURCHASE);
        transaction.setCoinAmount(pack.getPriceCoins());
        transaction.setDescription("Apertura de " + pack.getName());
        transaction.setCreatedAt(now);
        transaction.setCardRewards(rewards);

        return ensureSampleCards()
                .thenCompose(ignored -> userProfileRepository.registerPackPurchase(uid, transactionId, transaction, rewards))
                .thenApply(purchase -> new OpenPackResult(pack, purchase, rewards));
    }

    private CompletableFuture<Void> ensureSampleCards() {
        CompletableFuture<?>[] saves = SampleCatalog.cards().entrySet().stream()
                .map(entry -> cardRepository.save(entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(saves);
    }

    private PackProduct findPack(String packId) {
        return SampleCatalog.packs().stream()
                .filter(pack -> pack.getId().equals(packId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el sobre solicitado"));
    }

    private Map<String, Integer> drawRewards(PackProduct pack) {
        Map<String, Integer> rewards = new HashMap<>();
        List<String> possibleCardIds = pack.getPossibleCardIds();

        for (int i = 0; i < pack.getCardsPerPack(); i++) {
            String cardId = possibleCardIds.get(random.nextInt(possibleCardIds.size()));
            rewards.put(cardId, rewards.getOrDefault(cardId, 0) + 1);
        }

        return rewards;
    }
}
