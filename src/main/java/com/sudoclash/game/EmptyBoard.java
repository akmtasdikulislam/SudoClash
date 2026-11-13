package com.sudoclash.game;

public class EmptyBoard implements BoardView {
    @Override
    public int getValue(int row, int col) {
        return 0;
    }

    @Override
    public boolean isGiven(int row, int col) {
        return false;
    }

    @Override
    public boolean isWrong(int row, int col) {
        return false;
    }

    @Override
    public boolean hasNote(int row, int col, int value) {
        return false;
    }
}
