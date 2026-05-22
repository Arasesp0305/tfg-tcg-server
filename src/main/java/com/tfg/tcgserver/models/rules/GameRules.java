package com.tfg.tcgserver.models.rules;

public final class GameRules {

    public static final int TURN_SELECTION_SECONDS = 90;
    public static final int ACTION_CARDS_DRAWN_PER_TURN = 5;
    public static final int MAX_ACTIONS_PER_TURN = 2;
    public static final int SELECTED_CREATURES_PER_PLAYER = 3;
    public static final int ACTIVE_CREATURES_PER_PLAYER = 2;
    public static final int HIDDEN_CREATURES_PER_PLAYER = 1;
    public static final int DEAD_CREATURES_TO_LOSE = 3;

    private GameRules() {
    }
}
