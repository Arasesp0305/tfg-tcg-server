package com.tfg.tcgserver.models.game;

public class Move {

    private String playerId;
    private MoveType type;
    private String cardId;
    private String sourceFieldCardId;
    private String targetFieldCardId;
    private String createdAt;
    private int resolvedOrder;
    private int speed;

    public Move() {
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public MoveType getType() {
        return type;
    }

    public void setType(MoveType type) {
        this.type = type;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getSourceFieldCardId() {
        return sourceFieldCardId;
    }

    public void setSourceFieldCardId(String sourceFieldCardId) {
        this.sourceFieldCardId = sourceFieldCardId;
    }

    public String getTargetFieldCardId() {
        return targetFieldCardId;
    }

    public void setTargetFieldCardId(String targetFieldCardId) {
        this.targetFieldCardId = targetFieldCardId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getResolvedOrder() {
        return resolvedOrder;
    }

    public void setResolvedOrder(int resolvedOrder) {
        this.resolvedOrder = resolvedOrder;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
