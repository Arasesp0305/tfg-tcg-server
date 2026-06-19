package com.tfg.tcgserver.service.player;

import com.tfg.tcgserver.models.player.PurchaseTransaction;
import com.tfg.tcgserver.models.player.PurchaseTransactionType;
import com.tfg.tcgserver.models.player.UserOwnedCard;
import com.tfg.tcgserver.models.player.UserProfile;
import com.tfg.tcgserver.persistence.firebase.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PlayerService {

    private static final String INITIAL_ELO_ID = "bronze";
    private static final String DEFAULT_AVATAR_ID = "avatar_1";

    private final UserProfileRepository userProfileRepository;

    public PlayerService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public CompletableFuture<UserProfile> getProfile(String uid) {
        return userProfileRepository.findById(uid);
    }

    public CompletableFuture<UserProfile> createProfile(String uid, String username, String email) {
        String now = Instant.now().toString();

        UserProfile profile = new UserProfile();
        profile.setUsername(username);
        profile.setEmail(email);
        profile.setCoins(0);
        profile.setMmr(0);
        profile.setEloId(INITIAL_ELO_ID);
        profile.setAvatarId(DEFAULT_AVATAR_ID);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);

        return userProfileRepository.save(uid, profile)
                .thenApply(ignored -> profile);
    }

    public CompletableFuture<UserProfile> updateProfile(String uid, String username, String avatarId) {
        return userProfileRepository.findById(uid)
                .thenCompose(profile -> {
                    if (profile == null) {
                        CompletableFuture<UserProfile> failed = new CompletableFuture<>();
                        failed.completeExceptionally(new IllegalArgumentException("No existe el perfil del usuario"));
                        return failed;
                    }

                    profile.setUsername(username);
                    profile.setAvatarId(avatarId);
                    profile.setUpdatedAt(Instant.now().toString());

                    return userProfileRepository.save(uid, profile)
                            .thenApply(ignored -> profile);
                });
    }

    public CompletableFuture<Map<String, PurchaseTransaction>> getPurchaseHistory(String uid) {
        return userProfileRepository.findById(uid)
                .thenApply(profile -> {
                    if (profile == null) {
                        throw new IllegalArgumentException("No existe el perfil del usuario");
                    }

                    if (profile.getPurchaseHistory() == null) {
                        return Map.of();
                    }

                    return profile.getPurchaseHistory();
                });
    }

    public CompletableFuture<Map<String, UserOwnedCard>> getCollection(String uid) {
        return userProfileRepository.findById(uid)
                .thenApply(profile -> {
                    if (profile == null) {
                        throw new IllegalArgumentException("No existe el perfil del usuario");
                    }

                    if (profile.getCardCollection() == null) {
                        return Map.of();
                    }

                    return profile.getCardCollection();
                });
    }

    public CompletableFuture<PurchaseTransaction> registerPurchase(
            String uid,
            String itemId,
            String itemName,
            int coinAmount,
            String description
    ) {
        String transactionId = "purchase_" + UUID.randomUUID();
        String now = Instant.now().toString();

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(transactionId);
        transaction.setItemId(itemId);
        transaction.setItemName(itemName);
        transaction.setType(PurchaseTransactionType.PURCHASE);
        transaction.setCoinAmount(coinAmount);
        transaction.setDescription(description);
        transaction.setCreatedAt(now);

        return userProfileRepository.registerPurchase(uid, transactionId, transaction);
    }
}
