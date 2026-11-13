package com.sudoclash.data;

import com.sudoclash.sudoku.Difficulty;

public class SoloRecord extends GameRecord {
    private static final long serialVersionUID = 1L;

    public SoloRecord(String id, String username, Difficulty difficulty, Outcome outcome, int score, int seconds,
                      int mistakes, int hints, long playedAt) {
        super(id, username, difficulty, outcome, score, seconds, mistakes, hints, 0, 1, playedAt);
    }

    @Override
    public RecordMode getMode() {
        return RecordMode.SOLO;
    }

    // A finished puzzle is worth its score.
    @Override
    public int getPoints() {
        return isWin() ? getScore() : 0;
    }

    @Override
    public String getSummary() {
        return getMode().getLabel() + " - " + getDifficulty().getLabel() + " - " + getScore() + " pts - "
                + formatTime(getSeconds());
    }
}
