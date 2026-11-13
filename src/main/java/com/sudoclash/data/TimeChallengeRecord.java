package com.sudoclash.data;

import com.sudoclash.sudoku.Difficulty;

// Multilevel inheritance: GameRecord -> SoloRecord -> TimeChallengeRecord.
public class TimeChallengeRecord extends SoloRecord {
    private static final long serialVersionUID = 1L;

    public TimeChallengeRecord(String id, String username, Difficulty difficulty, Outcome outcome, int score,
                               int seconds, int mistakes, int hints, long playedAt) {
        super(id, username, difficulty, outcome, score, seconds, mistakes, hints, playedAt);
    }

    @Override
    public RecordMode getMode() {
        return RecordMode.TIME_CHALLENGE;
    }

    // Overriding: beating the clock gives the score, running out of time still gives half of it.
    @Override
    public int getPoints() {
        return isWin() ? super.getPoints() : getScore() / 2;
    }

    @Override
    public String getSummary() {
        String time = isWin() ? formatTime(getSeconds()) : "time up";
        return getMode().getLabel() + " - " + getDifficulty().getLabel() + " - " + getScore() + " pts - " + time;
    }
}
