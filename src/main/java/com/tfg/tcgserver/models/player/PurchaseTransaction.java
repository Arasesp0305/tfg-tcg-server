package com.tfg.tcgserver.models.player;

import java.util.HashMap;
import java.util.Map;

public class PurchaseTransaction {

    private String id;
    private String itemId;
    private String itemName;
    private PurchaseTransactionType type;
    private int coinAmount;
    private int balanceBefore;
    private int balanceAfter;
    private String description;
    private String createdAt;
    private Map<String, Integer> cardRewards = new HashMap<>();

    public PurchaseTransaction() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public PurchaseTransactionType getType() {
        return type;
    }

    public void setType(PurchaseTransactionType type) {
        this.type = type;
    }

    public int getCoinAmount() {
        return coinAmount;
    }

    public void setCoinAmount(int coinAmount) {
        this.coinAmount = coinAmount;
    }

    public int getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(int balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(int balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Integer> getCardRewards() {
        return cardRewards;
    }

    public void setCardRewards(Map<String, Integer> cardRewards) {
        this.cardRewards = cardRewards;
    }
}
