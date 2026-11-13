package com.sudoclash.game;

import java.util.UUID;

import com.sudoclash.sudoku.Difficulty;
import com.sudoclash.sudoku.Puzzle;

// Abstraction: a Game cannot be created itself, only its children (SoloGame, MultiplayerGame ...).
public abstract class Game {
    // Encapsulation: private fields, children use the getters.
    private final String id = UUID.randomUUID().toString();
    private final Puzzle puzzle;
    private volatile GameState state = GameState.NOT_STARTED;
    private long startMillis;
    private long endMillis;

    protected Game(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    public abstract String getModeName();

    public String getId() {
        return id;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public Difficulty getDifficulty() {
        return puzzle.getDifficulty();
    }

    public GameState getState() {
        return state;
    }

    // When the game ended (now, if it is still running).
    public synchronized long getEndMillis() {
        return endMillis > 0 ? endMillis : System.currentTimeMillis();
    }

    public synchronized void start() {
        if (state == GameState.NOT_STARTED) {
            state = GameState.RUNNING;
            startMillis = System.currentTimeMillis();
        }
    }

    protected synchronized void finish(GameState newState) {
        if (state == GameState.RUNNING) {
            state = newState;
            endMillis = System.currentTimeMillis();
        }
    }

    public synchronized void abandon() {
        finish(GameState.ABANDONED);
    }

    public synchronized int getElapsedSeconds() {
        if (state == GameState.NOT_STARTED) {
            return 0;
        }
        long end = (state == GameState.RUNNING) ? System.currentTimeMillis() : endMillis;
        return (int) ((end - startMillis) / 1000);
    }
}
