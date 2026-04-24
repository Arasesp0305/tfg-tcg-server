package com.tfg.tcgserver.models.game;

import java.util.ArrayList;
import java.util.List;

public class CreatureSelection {

    private List<String> activeCreatureCardIds = new ArrayList<>();
    private String hiddenCreatureCardId;
    private String submittedAt;

    public CreatureSelection() {
    }

    public List<String> getActiveCreatureCardIds() {
        return activeCreatureCardIds;
    }

    public void setActiveCreatureCardIds(List<String> activeCreatureCardIds) {
        this.activeCreatureCardIds = activeCreatureCardIds;
    }

    public String getHiddenCreatureCardId() {
        return hiddenCreatureCardId;
    }

    public void setHiddenCreatureCardId(String hiddenCreatureCardId) {
        this.hiddenCreatureCardId = hiddenCreatureCardId;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}
