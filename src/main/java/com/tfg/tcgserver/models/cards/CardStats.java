package com.tfg.tcgserver.models.cards;

public class CardStats {

    private int attack;
    private int health;
    private int cost;
    private int speed;

    public CardStats() {
    }

    public CardStats(int attack, int health, int cost, int speed) {
        this.attack = attack;
        this.health = health;
        this.cost = cost;
        this.speed = speed;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
