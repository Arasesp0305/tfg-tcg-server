package com.tfg.tcgserver.models.game;

import java.util.ArrayList;
import java.util.List;

public class GamePlayerState {

    private String deckId;
    private int deadCreatures;
    private List<String> actionDeck = new ArrayList<>();
    private List<String> hand = new ArrayList<>();
    private List<String> discardPile = new ArrayList<>();

    public GamePlayerState() {
    }

    public GamePlayerState(String deckId, int deadCreatures) {
        this.deckId = deckId;
        this.deadCreatures = deadCreatures;
    }

    public String getDeckId() {
        return deckId;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public int getDeadCreatures() {
        return deadCreatures;
    }

    public void setDeadCreatures(int deadCreatures) {
        this.deadCreatures = deadCreatures;
    }

    public List<String> getActionDeck() {
        return actionDeck;
    }

    public void setActionDeck(List<String> actionDeck) {
        this.actionDeck = actionDeck;
    }

    public List<String> getHand() {
        return hand;
    }

    public void setHand(List<String> hand) {
        this.hand = hand;
    }

    public List<String> getDiscardPile() {
        return discardPile;
    }

    public void setDiscardPile(List<String> discardPile) {
        this.discardPile = discardPile;
    }
}
