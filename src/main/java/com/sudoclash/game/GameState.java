package com.sudoclash.game;

public enum GameState {
    NOT_STARTED, RUNNING, COMPLETED, TIME_UP, ABANDONED;

    public boolean isFinished() {
        return this == COMPLETED || this == TIME_UP || this == ABANDONED;
    }
}
