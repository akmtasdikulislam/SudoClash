package com.sudoclash.sudoku;

public class SudokuSolver {
    private int solutionCount;
    private int limit;
    private int[][] firstSolution;

    public SudokuBoard solve(SudokuBoard board) {
        search(board, 1);
        if (firstSolution == null) {
            return null;
        }
        return new SudokuBoard(firstSolution);
    }

    public int countSolutions(SudokuBoard board, int limit) {
        search(board, limit);
        return solutionCount;
    }

    public boolean hasUniqueSolution(SudokuBoard board) {
        return countSolutions(board, 2) == 1;
    }

    private void search(SudokuBoard board, int limit) {
        solutionCount = 0;
        this.limit = limit;
        firstSolution = null;
        if (SudokuValidator.isBoardValid(board)) {
            fill(board.toArray());
        }
    }

    // Backtracking on the empty cell with the fewest candidates.
    private void fill(int[][] grid) {
        int bestRow = -1;
        int bestCol = -1;
        boolean[] bestCandidates = null;
        int bestCount = 10;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (grid[row][col] != 0) {
                    continue;
                }
                boolean[] candidates = SudokuValidator.getCandidates(grid, row, col);
                int count = SudokuValidator.countTrue(candidates);
                if (count == 0) {
                    return;
                }
                if (count < bestCount) {
                    bestCount = count;
                    bestRow = row;
                    bestCol = col;
                    bestCandidates = candidates;
                }
            }
        }

        if (bestRow == -1) {
            solutionCount++;
            if (firstSolution == null) {
                firstSolution = new SudokuBoard(grid).toArray();
            }
            return;
        }

        for (int value = 1; value <= 9; value++) {
            if (bestCandidates[value]) {
                grid[bestRow][bestCol] = value;
                fill(grid);
                grid[bestRow][bestCol] = 0;
                if (solutionCount >= limit) {
                    return;
                }
            }
        }
    }
}
