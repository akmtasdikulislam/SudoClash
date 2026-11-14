package com.sudoclash.game;

// The small set of stickers and short phrases players can send to each other in Face-Off.
// Only these ids are accepted by the server (a changed client can not send anything else).
public enum Reaction {
    LAUGH(1, "Haha", Kind.STICKER),
    APPLAUSE(2, "Applause", Kind.STICKER),
    THINKING(3, "Hmm...", Kind.STICKER),
    CELEBRATE(4, "Party!", Kind.STICKER),
    SURPRISE(5, "Wow!", Kind.STICKER),
    OOPS(6, "Oh no!", Kind.STICKER),
    NICE_MOVE(7, "Nice move!", Kind.PHRASE),
    YOUR_TURN(8, "Your turn!", Kind.PHRASE),
    ALMOST(9, "Almost got it!", Kind.PHRASE),
    GOOD_GAME(10, "Good game!", Kind.PHRASE);

    public enum Kind { STICKER, PHRASE }

    private final int id;
    private final String label;
    private final Kind kind;

    Reaction(int id, String label, Kind kind) {
        this.id = id;
        this.label = label;
        this.kind = kind;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Kind getKind() {
        return kind;
    }

    // null for an id that does not exist.
    public static Reaction fromId(int id) {
        for (Reaction reaction : values()) {
            if (reaction.id == id) {
                return reaction;
            }
        }
        return null;
    }
}
