package com.sudoclash.leaderboard;

import java.util.List;

import com.sudoclash.data.GameRecord;

// Every mode together: Clash Points decide the rank.
public class OverallBoard extends Leaderboard {
    public OverallBoard() {
        super("Overall", "Clash Points from every mode");
    }

    @Override
    public String[] getColumns() {
        return new String[] {"Clash Points", "Wins", "Games", "Win rate"};
    }

    @Override
    protected boolean accepts(GameRecord record) {
        return true;
    }

    @Override
    protected Entry summarize(String player, List<GameRecord> records) {
        int points = 0;
        int wins = 0;
        for (GameRecord record : records) {
            points += record.getPoints();
            if (record.isWin()) {
                wins++;
            }
        }
        return new Entry(player, points, wins, String.valueOf(points), String.valueOf(wins),
                String.valueOf(records.size()), percent(wins, records.size()) + "%");
    }
}
