package com.sudoclash.sudoku;

public class SudokuBoard {
    public static final int SIZE = 9;

    // 0 means an empty cell
    private final int[][] cells = new int[SIZE][SIZE];

    public SudokuBoard() {
    }

    public SudokuBoard(int[][] values) {
        if (values == null || values.length != SIZE) {
            throw new IllegalArgumentException("Board must be 9x9");
        }
        for (int row = 0; row < SIZE; row++) {
            if (values[row].length != SIZE) {
                throw new IllegalArgumentException("Board must be 9x9");
            }
            for (int col = 0; col < SIZE; col++) {
                setValue(row, col, values[row][col]);
            }
        }
    }

    public int getValue(int row, int col) {
        checkCell(row, col);
        return cells[row][col];
    }

    public void setValue(int row, int col, int value) {
        checkCell(row, col);
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be 0-9");
        }
        cells[row][col] = value;
    }

    public boolean isEmpty(int row, int col) {
        return getValue(row, col) == 0;
    }

    public boolean isFull() {
        return countFilled() == SIZE * SIZE;
    }

    public int countFilled() {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public SudokuBoard copy() {
        return new SudokuBoard(cells);
    }

    public int[][] toArray() {
        int[][] copy = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                copy[row][col] = cells[row][col];
            }
        }
        return copy;
    }

    public boolean sameAs(SudokuBoard other) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] != other.cells[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkCell(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IllegalArgumentException("Cell is outside the board");
        }
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < SIZE; row++) {
            if (row % 3 == 0 && row != 0) {
                text.append("------+-------+------\n");
            }
            for (int col = 0; col < SIZE; col++) {
                if (col % 3 == 0 && col != 0) {
                    text.append("| ");
                }
                text.append(cells[row][col] == 0 ? "." : String.valueOf(cells[row][col]));
                text.append(" ");
            }
            text.append("\n");
        }
        return text.toString();
    }
}
