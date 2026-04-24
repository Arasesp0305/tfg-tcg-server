package com.tfg.tcgserver.models.game;

import java.util.HashMap;
import java.util.Map;

public class Game {

    private GameStatus status;
    private String createdAt;
    private String startedAt;
    private String finishedAt;
    private String player1Id;
    private String player2Id;
    private String currentTurnId;
    private String winnerId;
    private Map<String, GamePlayerState> players = new HashMap<>();
    private Map<String, CreatureSelection> creatureSelections = new HashMap<>();
    private Map<String, Turn> turns = new HashMap<>();

    public Game() {
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public String getCurrentTurnId() {
        return currentTurnId;
    }

    public void setCurrentTurnId(String currentTurnId) {
        this.currentTurnId = currentTurnId;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public Map<String, GamePlayerState> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, GamePlayerState> players) {
        this.players = players;
    }

    public Map<String, CreatureSelection> getCreatureSelections() {
        return creatureSelections;
    }

    public void setCreatureSelections(Map<String, CreatureSelection> creatureSelections) {
        this.creatureSelections = creatureSelections;
    }

    public Map<String, Turn> getTurns() {
        return turns;
    }

    public void setTurns(Map<String, Turn> turns) {
        this.turns = turns;
    }
}
