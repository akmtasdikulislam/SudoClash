package com.sudoclash.game;

import java.util.List;

import com.sudoclash.data.GameRecord;

// Interface: a finished game can turn itself into records for the leaderboard.
// SoloGame and MultiplayerGame both implement it in their own way (polymorphism).
public interface Recordable {
    // Empty list when the game is not finished or should not be counted.
    List<GameRecord> createRecords();
}
