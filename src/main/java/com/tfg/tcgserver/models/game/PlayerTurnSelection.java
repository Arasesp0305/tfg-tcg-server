package com.tfg.tcgserver.models.game;

import java.util.HashMap;
import java.util.Map;

public class PlayerTurnSelection {

    private String submittedAt;
    private Map<String, Move> actions = new HashMap<>();

    public PlayerTurnSelection() {
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Map<String, Move> getActions() {
        return actions;
    }

    public void setActions(Map<String, Move> actions) {
        this.actions = actions;
    }
}
