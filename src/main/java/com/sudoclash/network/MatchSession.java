package com.sudoclash.network;

import java.util.List;

import com.sudoclash.game.CompeteGame;
import com.sudoclash.game.FaceOffGame;
import com.sudoclash.game.MatchMode;
import com.sudoclash.game.MultiplayerGame;
import com.sudoclash.sudoku.Puzzle;

// Abstraction + polymorphism: Room only knows "a match session". Whether it is Compete or Face-Off
// is decided at runtime by the real object (dynamic binding), so Room needs no instanceof checks.
abstract class MatchSession {
    private final Room room;

    protected MatchSession(Room room) {
        this.room = room;
    }

    protected Room getRoom() {
        return room;
    }

    abstract MultiplayerGame getGame();

    // Sends the first messages of the match.
    abstract void begin();

    // The server checks every move; the client is never trusted.
    abstract void handleMove(ClientHandler player, NetworkMessage message);

    // Only Face-Off has reactions.
    boolean allowsReactions() {
        return false;
    }

    // Stops anything that runs in the background (timers).
    void stop() {
    }

    // Factory method: picks the right session for the chosen mode.
    static MatchSession create(MatchMode mode, Room room, Puzzle puzzle, List<String> names, int turnSeconds) {
        if (mode == MatchMode.COMPETE) {
            return new CompeteSession(room, new CompeteGame(puzzle, names));
        }
        return new FaceOffSession(room, new FaceOffGame(puzzle, names, turnSeconds), turnSeconds);
    }
}
