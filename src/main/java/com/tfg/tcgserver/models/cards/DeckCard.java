package com.tfg.tcgserver.models.cards;

public class DeckCard {

    private String cardId;
    private int quantity;

    public DeckCard() {
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
}
