package com.sudoclash.sudoku;

public class Puzzle {
    private final SudokuBoard board;
    private final SudokuBoard solution;
    private final Difficulty difficulty;
    private final int logicLevel;

    public Puzzle(SudokuBoard board, SudokuBoard solution, Difficulty difficulty, int logicLevel) {
        this.board = board.copy();
        this.solution = solution.copy();
        this.difficulty = difficulty;
        this.logicLevel = logicLevel;
    }

    public SudokuBoard getBoard() {
        return board.copy();
    }

    public SudokuBoard getSolution() {
        return solution.copy();
    }

    public boolean isGiven(int row, int col) {
        return !board.isEmpty(row, col);
    }

    public int getClueCount() {
        return board.countFilled();
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getLogicLevel() {
        return logicLevel;
    }
}
