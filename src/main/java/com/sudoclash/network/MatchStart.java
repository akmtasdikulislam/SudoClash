package com.sudoclash.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sudoclash.game.MatchMode;

public class MatchStart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MatchMode mode;
    private final int[][] puzzle;
    private final List<String> players;
    private final int turnSeconds;

    public MatchStart(MatchMode mode, int[][] puzzle, List<String> players, int turnSeconds) {
        this.mode = mode;
        this.puzzle = puzzle;
        this.players = new ArrayList<>(players);
        this.turnSeconds = turnSeconds;
    }

    public MatchMode getMode() {
        return mode;
    }

    public int[][] getPuzzle() {
        return puzzle;
    }

    public List<String> getPlayers() {
        return new ArrayList<>(players);
    }

    public int getTurnSeconds() {
        return turnSeconds;
    }
}
