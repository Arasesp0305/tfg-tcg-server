package com.tfg.tcgserver.models.player;

import com.tfg.tcgserver.models.cards.DeckCard;

import java.util.HashMap;
import java.util.Map;

public class Deck {

    private String name;
    private Map<String, DeckCard> cards = new HashMap<>();
    private DeckCardCount cardCount = new DeckCardCount();
    private boolean playable;
    private String createdAt;
    private String updatedAt;

    public Deck() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, DeckCard> getCards() {
        return cards;
    }

    public void setCards(Map<String, DeckCard> cards) {
        this.cards = cards;
    }

    public DeckCardCount getCardCount() {
        return cardCount;
    }

    public void setCardCount(DeckCardCount cardCount) {
        this.cardCount = cardCount;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
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
}
