package com.sudoclash.sudoku;

public enum Technique {
    NAKED_SINGLE("Naked Single", 1),
    HIDDEN_SINGLE("Hidden Single", 1),
    POINTING_PAIR("Pointing Pair / Box-Line", 2),
    NAKED_PAIR("Naked Pair", 2),
    NAKED_TRIPLE("Naked Triple", 3),
    X_WING("X-Wing", 3);

    private final String label;
    private final int level;

    Technique(String label, int level) {
        this.label = label;
        this.level = level;
    }

    public String getLabel() {
        return label;
    }

    public int getLevel() {
        return level;
    }
}
