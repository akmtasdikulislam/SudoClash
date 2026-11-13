package com.sudoclash.game;

public class Move {
    private final int row;
    private final int col;
    private final int oldValue;
    private final int newValue;

    public Move(int row, int col, int oldValue, int newValue) {
        this.row = row;
        this.col = col;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }
}
