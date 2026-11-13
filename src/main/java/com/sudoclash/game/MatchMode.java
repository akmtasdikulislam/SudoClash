package com.sudoclash.game;

public enum MatchMode {
    COMPETE("Compete", 2, 4, "At least 2 players are required."),
    FACE_OFF("Face-Off", 2, 2, "Face-Off requires exactly 2 players.");

    private final String label;
    private final int minPlayers;
    private final int maxPlayers;
    private final String ruleMessage;

    MatchMode(String label, int minPlayers, int maxPlayers, String ruleMessage) {
        this.label = label;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.ruleMessage = ruleMessage;
    }

    public String getLabel() {
        return label;
    }

    public boolean isValidPlayerCount(int count) {
        return count >= minPlayers && count <= maxPlayers;
    }

    public String getRuleMessage() {
        return ruleMessage;
    }

    @Override
    public String toString() {
        return label;
    }
}
