package com.tfg.tcgserver.models.game;

public class FieldCard {

    private String ownerId;
    private String cardId;
    private int currentHealth;
    private int maxHealth;
    private int attack;
    private int speed;
    private int position;
    private FieldCardVisibility visibility;
    private FieldCardStatus status;

    public FieldCard() {
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public FieldCardVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(FieldCardVisibility visibility) {
        this.visibility = visibility;
    }

    public FieldCardStatus getStatus() {
        return status;
    }

    public void setStatus(FieldCardStatus status) {
        this.status = status;
    }
}
