package com.tfg.tcgserver.models.player;

public class UserOwnedCard {

    private String cardId;
    private int quantity;
    private String firstObtainedAt;
    private String updatedAt;

    public UserOwnedCard() {
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFirstObtainedAt() {
        return firstObtainedAt;
    }

    public void setFirstObtainedAt(String firstObtainedAt) {
        this.firstObtainedAt = firstObtainedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
