package com.sudoclash.game;

// Interface for anything that has a score (SoloGame implements it).
public interface Scorable {
    int getCurrentScore();

    int getFinalScore();

    // Default method: implementing classes get it for free.
    default String describeScore() {
        return getFinalScore() + " points";
    }
}
