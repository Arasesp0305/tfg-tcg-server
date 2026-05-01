package com.tfg.tcgserver.matchmaking;

public class MatchmakingEntry {

    private final String playerId;
    private final String deckId;
    private final int mmr;
    private final String joinedAt;

    public MatchmakingEntry(String playerId, String deckId, int mmr, String joinedAt) {
        this.playerId = playerId;
        this.deckId = deckId;
        this.mmr = mmr;
        this.joinedAt = joinedAt;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDeckId() {
        return deckId;
    }

    public int getMmr() {
        return mmr;
    }

    public String getJoinedAt() {
        return joinedAt;
    }
}
