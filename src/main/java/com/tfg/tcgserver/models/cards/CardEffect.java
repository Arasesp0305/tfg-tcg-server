package com.tfg.tcgserver.models.cards;

public class CardEffect {

    private EffectTarget target;
    private int damage;
    private int cost;

    public CardEffect() {
    }

    public CardEffect(EffectTarget target, int damage, int cost) {
        this.target = target;
        this.damage = damage;
        this.cost = cost;
    }

    public EffectTarget getTarget() {
        return target;
    }

    public void setTarget(EffectTarget target) {
        this.target = target;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
