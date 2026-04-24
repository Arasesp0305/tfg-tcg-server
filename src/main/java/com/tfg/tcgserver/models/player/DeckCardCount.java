package com.tfg.tcgserver.models.player;

public class DeckCardCount {

    private int action;
    private int creature;
    private int total;

    public DeckCardCount() {
    }

    public DeckCardCount(int action, int creature, int total) {
        this.action = action;
        this.creature = creature;
        this.total = total;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getCreature() {
        return creature;
    }

    public void setCreature(int creature) {
        this.creature = creature;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
