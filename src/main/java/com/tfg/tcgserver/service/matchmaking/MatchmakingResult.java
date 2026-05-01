package com.tfg.tcgserver.service.matchmaking;

public class MatchmakingResult {

    private final boolean matched;
    private final String gameId;
    private final int queueSize;

    private MatchmakingResult(boolean matched, String gameId, int queueSize) {
        this.matched = matched;
        this.gameId = gameId;
        this.queueSize = queueSize;
    }

    public static MatchmakingResult waiting(int queueSize) {
        return new MatchmakingResult(false, null, queueSize);
    }

    public static MatchmakingResult matched(String gameId) {
        return new MatchmakingResult(true, gameId, 0);
    }

    public boolean isMatched() {
        return matched;
    }

    public String getGameId() {
        return gameId;
    }

    public int getQueueSize() {
        return queueSize;
    }
}
