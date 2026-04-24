package com.tfg.tcgserver.models.game;

import java.util.HashMap;
import java.util.Map;

public class Turn {

    private int number;
    private String startedAt;
    private String selectionDeadlineAt;
    private String endedAt;
    private Map<String, FieldCard> fieldCards = new HashMap<>();
    private Map<String, PlayerTurnSelection> playerSelections = new HashMap<>();
    private Map<String, Move> resolvedMoves = new HashMap<>();

    public Turn() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getSelectionDeadlineAt() {
        return selectionDeadlineAt;
    }

    public void setSelectionDeadlineAt(String selectionDeadlineAt) {
        this.selectionDeadlineAt = selectionDeadlineAt;
    }

    public String getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(String endedAt) {
        this.endedAt = endedAt;
    }

    public Map<String, FieldCard> getFieldCards() {
        return fieldCards;
    }

    public void setFieldCards(Map<String, FieldCard> fieldCards) {
        this.fieldCards = fieldCards;
    }

    public Map<String, PlayerTurnSelection> getPlayerSelections() {
        return playerSelections;
    }

    public void setPlayerSelections(Map<String, PlayerTurnSelection> playerSelections) {
        this.playerSelections = playerSelections;
    }

    public Map<String, Move> getResolvedMoves() {
        return resolvedMoves;
    }

    public void setResolvedMoves(Map<String, Move> resolvedMoves) {
        this.resolvedMoves = resolvedMoves;
    }
}
