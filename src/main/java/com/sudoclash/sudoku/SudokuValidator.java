package com.sudoclash.sudoku;

public class SudokuValidator {
    public static boolean canPlace(int[][] grid, int row, int col, int value) {
        for (int i = 0; i < 9; i++) {
            if (i != col && grid[row][i] == value) {
                return false;
            }
            if (i != row && grid[i][col] == value) {
                return false;
            }
        }

        int boxRow = row / 3 * 3;
        int boxCol = col / 3 * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if ((r != row || c != col) && grid[r][c] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isBoardValid(SudokuBoard board) {
        int[][] grid = board.toArray();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (grid[row][col] != 0 && !canPlace(grid, row, col, grid[row][col])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSolved(SudokuBoard board) {
        return board.isFull() && isBoardValid(board);
    }

    public static boolean[] getCandidates(int[][] grid, int row, int col) {
        boolean[] candidates = new boolean[10];
        if (grid[row][col] != 0) {
            return candidates;
        }
        for (int value = 1; value <= 9; value++) {
            candidates[value] = canPlace(grid, row, col, value);
        }
        return candidates;
    }

    public static int countTrue(boolean[] candidates) {
        int count = 0;
        for (int value = 1; value <= 9; value++) {
            if (candidates[value]) {
                count++;
            }
        }
        return count;
    }
}
