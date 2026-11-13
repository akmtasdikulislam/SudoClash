package com.sudoclash.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MatchMode mode;
    private final List<String> standings;
    private final String winner;
    private final String reason;
    private final Map<String, String> details;

    public MatchResult(MatchMode mode, List<String> standings, String winner, String reason,
                       Map<String, String> details) {
        this.mode = mode;
        this.standings = new ArrayList<>(standings);
        this.winner = winner;
        this.reason = reason;
        this.details = new LinkedHashMap<>(details);
    }

    public MatchMode getMode() {
        return mode;
    }

    public List<String> getStandings() {
        return new ArrayList<>(standings);
    }

    public String getWinner() {
        return winner;
    }

    public String getReason() {
        return reason;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public int getPlace(String player) {
        int index = standings.indexOf(player);
        return index < 0 ? -1 : index + 1;
    }
}
