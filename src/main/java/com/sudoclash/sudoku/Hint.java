package com.sudoclash.sudoku;

public class Hint {
    private final Technique technique;
    private final int row;
    private final int col;
    // 0 for a step that only removes candidates
    private final int value;
    private final String message;

    public Hint(Technique technique, int row, int col, int value, String message) {
        this.technique = technique;
        this.row = row;
        this.col = col;
        this.value = value;
        this.message = message;
    }

    public Technique getTechnique() {
        return technique;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPlacement() {
        return value > 0;
    }

    @Override
    public String toString() {
        return technique.getLabel() + ": " + message;
    }
}
