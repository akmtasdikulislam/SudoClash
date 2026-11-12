package com.sudoclash.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sudoclash.sudoku.Difficulty;

// Real numbers for the profile screen, calculated from the saved game records.
public class PlayerStats {
    private final String username;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int draws;
    private int totalPoints;
    private int perfectGames;
    private int totalMistakes;
    private int totalHints;
    private int currentStreak;
    private int bestStreak;
    private final Map<String, Integer> bestScores = new HashMap<>();
    private final Map<String, Integer> fastestTimes = new HashMap<>();
    private final Map<RecordMode, int[]> perMode = new HashMap<>();
    private final List<GameRecord> recent = new ArrayList<>();

    public PlayerStats(String username, List<GameRecord> records) {
        this.username = username;
        List<GameRecord> sorted = new ArrayList<>(records);
        Collections.sort(sorted);
        for (GameRecord record : sorted) {
            add(record);
        }
        for (int i = sorted.size() - 1; i >= 0 && recent.size() < 5; i--) {
            recent.add(sorted.get(i));
        }
    }

    private void add(GameRecord record) {
        gamesPlayed++;
        totalPoints += record.getPoints();
        totalMistakes += record.getMistakes();
        totalHints += record.getHints();

        int[] counts = perMode.computeIfAbsent(record.getMode(), mode -> new int[2]);
        counts[0]++;

        switch (record.getOutcome()) {
            case WIN:
                wins++;
                counts[1]++;
                currentStreak++;
                bestStreak = Math.max(bestStreak, currentStreak);
                if (record.getMistakes() == 0) {
                    perfectGames++;
                }
                remember(bestScores, record, record.getScore(), true);
                if (!record.getMode().isMultiplayer()) {
                    remember(fastestTimes, record, record.getSeconds(), false);
                }
                break;
            case DRAW:
                draws++;
                break;
            default:
                losses++;
                currentStreak = 0;
                break;
        }
    }

    private static String key(RecordMode mode, Difficulty difficulty) {
        return mode.name() + ":" + difficulty.name();
    }

    private static void remember(Map<String, Integer> map, GameRecord record, int value, boolean higherIsBetter) {
        if (record.getMode().isMultiplayer()) {
            return;
        }
        String key = key(record.getMode(), record.getDifficulty());
        Integer old = map.get(key);
        if (old == null || (higherIsBetter ? value > old : value < old)) {
            map.put(key, value);
        }
    }

    public String getUsername() {
        return username;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public int getWinRate() {
        return gamesPlayed == 0 ? 0 : wins * 100 / gamesPlayed;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getPerfectGames() {
        return perfectGames;
    }

    public int getTotalMistakes() {
        return totalMistakes;
    }

    public int getTotalHints() {
        return totalHints;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    // 0 means "no result yet".
    public int getBestScore(RecordMode mode, Difficulty difficulty) {
        return bestScores.getOrDefault(key(mode, difficulty), 0);
    }

    public int getFastestTime(RecordMode mode, Difficulty difficulty) {
        return fastestTimes.getOrDefault(key(mode, difficulty), 0);
    }

    public int getPlayed(RecordMode mode) {
        int[] counts = perMode.get(mode);
        return counts == null ? 0 : counts[0];
    }

    public int getWins(RecordMode mode) {
        int[] counts = perMode.get(mode);
        return counts == null ? 0 : counts[1];
    }

    // The newest games first (at most 5).
    public List<GameRecord> getRecent() {
        return new ArrayList<>(recent);
    }
}
