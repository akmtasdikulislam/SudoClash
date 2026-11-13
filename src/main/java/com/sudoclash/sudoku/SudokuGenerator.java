package com.sudoclash.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.sudoclash.exception.GenerationException;

public class SudokuGenerator {
    // The search is bounded: at most this many boards, and at most this much time.
    public static final int MAX_ATTEMPTS = 150;
    private static final long TIME_LIMIT_MILLIS = 12_000;

    private final Random random;
    private final SudokuSolver solver = new SudokuSolver();

    public SudokuGenerator() {
        random = new Random();
    }

    public SudokuGenerator(long seed) {
        random = new Random(seed);
    }

    // Slow (it solves many boards): do not call it on the Swing thread.
    // Every puzzle that is returned has been checked (see verify). If no board fits the rules within the
    // limits, an exception is thrown: a puzzle with the wrong difficulty is never returned.
    public Puzzle generate(Difficulty difficulty) throws GenerationException {
        return generate(difficulty, difficulty.getClues());
    }

    // Overloading: the clue count can be chosen (used to compare how easy different clue counts feel).
    public Puzzle generate(Difficulty difficulty, int clues) throws GenerationException {
        long deadline = System.currentTimeMillis() + TIME_LIMIT_MILLIS;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS && System.currentTimeMillis() < deadline; attempt++) {
            SudokuBoard solution = createFullBoard();
            SudokuBoard board = removeNumbers(solution, clues);
            if (countClues(board) != clues) {
                continue;
            }
            // 0 = the puzzle can not be solved with the allowed human techniques.
            int level = new HintFinder(board).solveWithTechniques(difficulty.getMaxTechniqueLevel());
            if (level < difficulty.getMinTechniqueLevel() || level > difficulty.getMaxTechniqueLevel()) {
                continue;
            }
            Puzzle puzzle = new Puzzle(board, solution, difficulty, level);
            if (verify(puzzle, clues) == null) {
                return puzzle;
            }
        }
        throw new GenerationException("Could not create a " + difficulty.getLabel()
                + " puzzle right now. Please try again.");
    }

    // null when the puzzle is correct, otherwise the reason. Checks the solution, the clues, that the
    // solution is the only one, the clue count and the difficulty (technique level).
    public static String verify(Puzzle puzzle) {
        return verify(puzzle, puzzle.getDifficulty().getClues());
    }

    public static String verify(Puzzle puzzle, int expectedClues) {
        SudokuBoard solution = puzzle.getSolution();
        SudokuBoard board = puzzle.getBoard();
        int[][] full = solution.toArray();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = full[row][col];
                if (value < 1 || value > 9) {
                    return "the solution has an empty or invalid cell";
                }
                full[row][col] = 0;
                boolean fits = SudokuValidator.canPlace(full, row, col, value);
                full[row][col] = value;
                if (!fits) {
                    return "the solution breaks a Sudoku rule";
                }
                if (board.getValue(row, col) != 0 && board.getValue(row, col) != value) {
                    return "a clue is different from the solution";
                }
            }
        }
        if (countClues(board) != expectedClues) {
            return "wrong clue count: " + countClues(board) + " instead of " + expectedClues;
        }
        if (!new SudokuSolver().hasUniqueSolution(board)) {
            return "the puzzle does not have exactly one solution";
        }
        Difficulty difficulty = puzzle.getDifficulty();
        int level = new HintFinder(board).solveWithTechniques(difficulty.getMaxTechniqueLevel());
        if (level < difficulty.getMinTechniqueLevel() || level > difficulty.getMaxTechniqueLevel()) {
            return "the puzzle needs technique level " + level + ", not " + difficulty.getMinTechniqueLevel()
                    + "-" + difficulty.getMaxTechniqueLevel();
        }
        return null;
    }

    public static int countClues(SudokuBoard board) {
        int count = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board.getValue(row, col) != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private SudokuBoard createFullBoard() {
        int[][] grid = new int[9][9];
        fillCell(grid, 0);
        return new SudokuBoard(grid);
    }

    private boolean fillCell(int[][] grid, int position) {
        if (position == 81) {
            return true;
        }
        int row = position / 9;
        int col = position % 9;

        List<Integer> numbers = new ArrayList<>();
        for (int value = 1; value <= 9; value++) {
            numbers.add(value);
        }
        Collections.shuffle(numbers, random);

        for (int value : numbers) {
            if (SudokuValidator.canPlace(grid, row, col, value)) {
                grid[row][col] = value;
                if (fillCell(grid, position + 1)) {
                    return true;
                }
                grid[row][col] = 0;
            }
        }
        return false;
    }

    // A number is only removed if the puzzle still has exactly one solution.
    private SudokuBoard removeNumbers(SudokuBoard solution, int targetClues) {
        SudokuBoard board = solution.copy();
        List<Integer> cells = new ArrayList<>();
        for (int i = 0; i < 81; i++) {
            cells.add(i);
        }
        Collections.shuffle(cells, random);

        int clues = 81;
        for (int index : cells) {
            if (clues <= targetClues) {
                break;
            }
            int row = index / 9;
            int col = index % 9;
            int old = board.getValue(row, col);
            board.setValue(row, col, 0);
            if (solver.hasUniqueSolution(board)) {
                clues--;
            } else {
                board.setValue(row, col, old);
            }
        }
        return board;
    }
}
