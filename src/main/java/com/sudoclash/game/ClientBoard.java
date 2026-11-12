package com.sudoclash.game;

import java.util.Stack;

import com.sudoclash.sudoku.SudokuBoard;

public class ClientBoard implements BoardView {
    private final int[][] values = new int[9][9];
    private final boolean[][] given = new boolean[9][9];
    private final boolean[][] wrong = new boolean[9][9];
    private final boolean[][][] notes = new boolean[9][9][10];
    private final int[][] owners = new int[9][9];
    private final Stack<Move> undoStack = new Stack<>();

    public ClientBoard(int[][] puzzle) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                values[row][col] = puzzle[row][col];
                given[row][col] = puzzle[row][col] != 0;
            }
        }
    }

    @Override
    public int getValue(int row, int col) {
        return values[row][col];
    }

    @Override
    public boolean isGiven(int row, int col) {
        return given[row][col];
    }

    @Override
    public boolean isWrong(int row, int col) {
        return wrong[row][col];
    }

    @Override
    public int getOwner(int row, int col) {
        return owners[row][col];
    }

    // Face-Off: the server sends who owns which cell with every board update.
    public void replaceOwners(int[][] newOwners) {
        if (newOwners == null) {
            return;
        }
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                owners[row][col] = newOwners[row][col];
            }
        }
    }

    @Override
    public boolean hasNote(int row, int col, int value) {
        return notes[row][col][value];
    }

    // Shown at once; the server tells us later whether it was right.
    public void setValue(int row, int col, int value) {
        undoStack.push(new Move(row, col, values[row][col], value));
        values[row][col] = value;
        wrong[row][col] = false;
        if (value != 0) {
            clearNotes(row, col);
        }
    }

    public Move undo() {
        if (undoStack.isEmpty()) {
            return null;
        }
        Move move = undoStack.pop();
        values[move.getRow()][move.getCol()] = move.getOldValue();
        wrong[move.getRow()][move.getCol()] = false;
        return move;
    }

    public void markResult(int row, int col, int value, boolean correct) {
        if (values[row][col] == value && value != 0) {
            wrong[row][col] = !correct;
        }
    }

    public void toggleNote(int row, int col, int value) {
        if (!given[row][col] && values[row][col] == 0 && value >= 1 && value <= 9) {
            notes[row][col][value] = !notes[row][col][value];
        }
    }

    public void clearNotes(int row, int col) {
        for (int value = 1; value <= 9; value++) {
            notes[row][col][value] = false;
        }
    }

    // Face-Off: the server sends the whole shared board.
    public void replaceValues(int[][] serverBoard) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                values[row][col] = serverBoard[row][col];
                if (values[row][col] != 0) {
                    clearNotes(row, col);
                }
            }
        }
    }

    public SudokuBoard toCleanBoard() {
        SudokuBoard board = new SudokuBoard();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (!wrong[row][col]) {
                    board.setValue(row, col, values[row][col]);
                }
            }
        }
        return board;
    }
}
