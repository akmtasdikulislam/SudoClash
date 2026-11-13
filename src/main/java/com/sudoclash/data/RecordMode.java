package com.sudoclash.data;

// Which kind of game a record belongs to.
public enum RecordMode {
    SOLO("Solo", false),
    TIME_CHALLENGE("Time Challenge", false),
    COMPETE("Compete", true),
    FACE_OFF("Face-Off", true);

    private final String label;
    private final boolean multiplayer;

    RecordMode(String label, boolean multiplayer) {
        this.label = label;
        this.multiplayer = multiplayer;
    }

    public String getLabel() {
        return label;
    }

    public boolean isMultiplayer() {
        return multiplayer;
    }

    @Override
    public String toString() {
        return label;
    }
}
