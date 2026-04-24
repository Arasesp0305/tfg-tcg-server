package com.tfg.tcgserver.models.player;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {

    private String username;
    private String email;
    private int coins;
    private int mmr;
    private String eloId;
    private String selectedDeckId;
    private String createdAt;
    private String updatedAt;
    private Map<String, Deck> decks = new HashMap<>();
    private Map<String, PurchaseTransaction> purchaseHistory = new HashMap<>();
    private Map<String, UserOwnedCard> cardCollection = new HashMap<>();

    public UserProfile() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getMmr() {
        return mmr;
    }

    public void setMmr(int mmr) {
        this.mmr = mmr;
    }

    public String getEloId() {
        return eloId;
    }

    public void setEloId(String eloId) {
        this.eloId = eloId;
    }

    public String getSelectedDeckId() {
        return selectedDeckId;
    }

    public void setSelectedDeckId(String selectedDeckId) {
        this.selectedDeckId = selectedDeckId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Deck> getDecks() {
        return decks;
    }

    public void setDecks(Map<String, Deck> decks) {
        this.decks = decks;
    }

    public Map<String, PurchaseTransaction> getPurchaseHistory() {
        return purchaseHistory;
    }

    public void setPurchaseHistory(Map<String, PurchaseTransaction> purchaseHistory) {
        this.purchaseHistory = purchaseHistory;
    }

    public Map<String, UserOwnedCard> getCardCollection() {
        return cardCollection;
    }

    public void setCardCollection(Map<String, UserOwnedCard> cardCollection) {
        this.cardCollection = cardCollection;
    }
}
