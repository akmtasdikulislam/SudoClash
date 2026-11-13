package com.sudoclash.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.sudoclash.data.GameRecord;
import com.sudoclash.data.Outcome;
import com.sudoclash.data.SoloRecord;
import com.sudoclash.exception.GenerationException;
import com.sudoclash.exception.InvalidMoveException;
import com.sudoclash.sudoku.Difficulty;
import com.sudoclash.sudoku.Hint;
import com.sudoclash.sudoku.HintFinder;
import com.sudoclash.sudoku.Puzzle;
import com.sudoclash.sudoku.SudokuBoard;
import com.sudoclash.sudoku.SudokuGenerator;

// Synchronized methods: in Time Challenge the timer thread and the Swing thread use the same game.
// Interfaces: BoardView (what the board shows), Scorable (score) and Recordable (leaderboard records).
public class SoloGame extends Game implements BoardView, Scorable, Recordable {
    public enum CellCheck { EMPTY, CORRECT, WRONG }

    // Encapsulation: every field is private. Children use getMistakeCount() and friends.
    private final SudokuBoard board;
    private final SudokuBoard solution;
    private final boolean[][][] notes = new boolean[9][9][10];
    private final boolean[][] hintCells = new boolean[9][9];
    private final Stack<Move> undoStack = new Stack<>();
    private int mistakes;
    private int hintsUsed;
    private String playerName;

    public SoloGame(Puzzle puzzle) {
        super(puzzle);
        this.board = puzzle.getBoard();
        this.solution = puzzle.getSolution();
    }

    public SoloGame(Difficulty difficulty) throws GenerationException {
        this(new SudokuGenerator().generate(difficulty));
    }

    @Override
    public String getModeName() {
        return "Solo";
    }

    // Overridden by TimeChallengeGame.
    protected void onMistake() {
    }

    public synchronized boolean makeMove(int row, int col, int value) throws InvalidMoveException {
        checkCanEdit(row, col);
        if (value < 0 || value > 9) {
            throw new InvalidMoveException("Number must be between 0 and 9.");
        }
        int oldValue = board.getValue(row, col);
        boolean correct = (value == 0) || (value == solution.getValue(row, col));
        if (oldValue == value) {
            return correct;
        }

        board.setValue(row, col, value);
        hintCells[row][col] = false;
        undoStack.push(new Move(row, col, oldValue, value));
        if (value != 0) {
            clearNotes(row, col);
            if (correct) {
                removeNoteFromPeers(row, col, value);
            }
        }

        if (!correct) {
            // A mistake stays counted even after Undo.
            mistakes++;
            onMistake();
        } else if (board.sameAs(solution)) {
            finish(GameState.COMPLETED);
        }
        return correct;
    }

    // Method overloading (compile-time polymorphism): same name, different parameters.
    public synchronized boolean makeMove(Move move) throws InvalidMoveException {
        return makeMove(move.getRow(), move.getCol(), move.getNewValue());
    }

    public synchronized boolean undo() throws InvalidMoveException {
        if (getState() != GameState.RUNNING) {
            throw new InvalidMoveException("The game is not running.");
        }
        if (undoStack.isEmpty()) {
            return false;
        }
        Move move = undoStack.pop();
        board.setValue(move.getRow(), move.getCol(), move.getOldValue());
        hintCells[move.getRow()][move.getCol()] = false;
        return true;
    }

    public synchronized void toggleNote(int row, int col, int value) throws InvalidMoveException {
        checkCanEdit(row, col);
        if (value < 1 || value > 9) {
            throw new InvalidMoveException("Number must be between 1 and 9.");
        }
        if (board.getValue(row, col) != 0) {
            throw new InvalidMoveException("Pencil marks go in empty cells.");
        }
        notes[row][col][value] = !notes[row][col][value];
    }

    public synchronized void clearNotesOfCell(int row, int col) throws InvalidMoveException {
        checkCanEdit(row, col);
        clearNotes(row, col);
    }

    // Hints are limited: this many per puzzle (the Hint button stops working after that).
    public static final int MAX_HINTS = 3;

    public int getHintsLeft() {
        return Math.max(0, MAX_HINTS - getHintsUsed());
    }

    public synchronized Hint getHint() throws InvalidMoveException {
        if (getState() != GameState.RUNNING) {
            throw new InvalidMoveException("The game is not running.");
        }
        // Wrong numbers are ignored, so the hint is based on correct cells only.
        SudokuBoard cleanBoard = board.copy();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (isWrong(row, col)) {
                    cleanBoard.setValue(row, col, 0);
                }
            }
        }
        Hint hint = new HintFinder(cleanBoard).findHint(3);
        if (hint != null) {
            hintsUsed++;
        }
        return hint;
    }

    // The Hint button: finds the next logical step, fills the correct number straight into the cell and returns
    // {row, col, value}. If no logical step is found it fills the first empty or wrong cell instead, so a hint always
    // helps while the puzzle is not finished. Costs one hint (see Difficulty). Returns null when nothing is left to fill.
    public synchronized int[] useHint() throws InvalidMoveException {
        if (getState() != GameState.RUNNING) {
            return null;
        }
        if (hintsUsed >= MAX_HINTS) {
            throw new InvalidMoveException("No hints left. You can use " + MAX_HINTS + " hints per puzzle.");
        }
        Hint hint = getHint();
        if (hint != null) {
            applyHint(hint);
            return new int[] {hint.getRow(), hint.getCol(), hint.getValue()};
        }
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int right = solution.getValue(row, col);
                if (board.getValue(row, col) != right) {
                    hintsUsed++;
                    makeMove(row, col, right);
                    hintCells[row][col] = true;
                    return new int[] {row, col, right};
                }
            }
        }
        return null;
    }

    // Puts the correct number of a hint on the board and remembers that this cell was filled by a hint.
    // The hint itself was already paid for in getHint(), so placing it never costs another penalty.
    public synchronized void applyHint(Hint hint) throws InvalidMoveException {
        makeMove(hint.getRow(), hint.getCol(), hint.getValue());
        hintCells[hint.getRow()][hint.getCol()] = true;
    }

    @Override
    public synchronized boolean isHint(int row, int col) {
        return hintCells[row][col] && board.getValue(row, col) != 0;
    }

    public synchronized CellCheck checkCell(int row, int col) {
        int value = board.getValue(row, col);
        if (value == 0) {
            return CellCheck.EMPTY;
        }
        return value == solution.getValue(row, col) ? CellCheck.CORRECT : CellCheck.WRONG;
    }

    private void checkCanEdit(int row, int col) throws InvalidMoveException {
        if (getState() != GameState.RUNNING) {
            throw new InvalidMoveException("The game is not running.");
        }
        if (row < 0 || row > 8 || col < 0 || col > 8) {
            throw new InvalidMoveException("That cell is outside the board.");
        }
        if (getPuzzle().isGiven(row, col)) {
            throw new InvalidMoveException("Starting numbers cannot be changed.");
        }
    }

    private void clearNotes(int row, int col) {
        for (int value = 1; value <= 9; value++) {
            notes[row][col][value] = false;
        }
    }

    private void removeNoteFromPeers(int row, int col, int value) {
        for (int i = 0; i < 9; i++) {
            notes[row][i][value] = false;
            notes[i][col][value] = false;
        }
        int boxRow = row / 3 * 3;
        int boxCol = col / 3 * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                notes[r][c][value] = false;
            }
        }
    }

    @Override
    public int getValue(int row, int col) {
        return board.getValue(row, col);
    }

    @Override
    public boolean isGiven(int row, int col) {
        return getPuzzle().isGiven(row, col);
    }

    @Override
    public boolean isWrong(int row, int col) {
        int value = board.getValue(row, col);
        return value != 0 && !getPuzzle().isGiven(row, col) && value != solution.getValue(row, col);
    }

    @Override
    public boolean hasNote(int row, int col, int value) {
        return notes[row][col][value];
    }

    public boolean hasAnyNote(int row, int col) {
        for (int value = 1; value <= 9; value++) {
            if (notes[row][col][value]) {
                return true;
            }
        }
        return false;
    }

    public int getMistakeCount() {
        return mistakes;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public int getBaseScore() {
        return getDifficulty().getBaseScore();
    }

    // The penalty per wrong entry and per hint depends on the difficulty (see Difficulty).
    public int getMistakePenalty() {
        return mistakes * getDifficulty().getWrongEntryPenalty();
    }

    public int getHintPenalty() {
        return hintsUsed * getDifficulty().getHintPenalty();
    }

    public int getProgressPercent() {
        int total = 81 - getPuzzle().getClueCount();
        int done = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (!getPuzzle().isGiven(row, col) && board.getValue(row, col) == solution.getValue(row, col)) {
                    done++;
                }
            }
        }
        return total == 0 ? 100 : done * 100 / total;
    }

    @Override
    public int getCurrentScore() {
        return Math.max(0, getBaseScore() - getMistakePenalty() - getHintPenalty());
    }

    @Override
    public int getFinalScore() {
        return getCurrentScore();
    }

    public String getPlayerName() {
        return playerName;
    }

    // Setter: the screen tells the game who is playing.
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    // Overridden by TimeChallengeGame.
    public boolean isTimed() {
        return false;
    }

    // -1 means "this mode has no countdown"; TimeChallengeGame overrides it.
    public int getRemainingSeconds() {
        return -1;
    }

    // Dynamic binding: the result screen calls this and the real object decides what is shown.
    public List<ResultLine> getResultLines() {
        List<ResultLine> lines = new ArrayList<>();
        lines.add(new ResultLine("Mode", getModeName()));
        lines.add(new ResultLine("Difficulty", getDifficulty().getLabel().toUpperCase()));
        lines.add(new ResultLine("Base score", String.valueOf(getBaseScore())));
        lines.add(new ResultLine("Mistakes", String.valueOf(mistakes)));
        lines.addAll(mistakeLines());
        lines.add(new ResultLine("Hints", String.valueOf(hintsUsed)));
        lines.add(new ResultLine("Hint penalty", penaltyText(getHintPenalty()), toneOf(getHintPenalty())));
        lines.addAll(timeLines());
        return lines;
    }

    // Hook methods: TimeChallengeGame overrides them.
    protected List<ResultLine> mistakeLines() {
        List<ResultLine> lines = new ArrayList<>();
        lines.add(new ResultLine("Mistake penalty", penaltyText(getMistakePenalty()), toneOf(getMistakePenalty())));
        return lines;
    }

    protected List<ResultLine> timeLines() {
        List<ResultLine> lines = new ArrayList<>();
        lines.add(new ResultLine("Time", GameTimer.format(getElapsedSeconds())));
        return lines;
    }

    protected static String penaltyText(int value) {
        return value == 0 ? "0" : "-" + value;
    }

    protected static ResultLine.Tone toneOf(int penalty) {
        return penalty == 0 ? ResultLine.Tone.NEUTRAL : ResultLine.Tone.BAD;
    }

    @Override
    public List<GameRecord> createRecords() {
        List<GameRecord> records = new ArrayList<>();
        Outcome outcome = null;
        if (getState() == GameState.COMPLETED) {
            outcome = Outcome.WIN;
        } else if (getState() == GameState.TIME_UP) {
            outcome = Outcome.LOSS;
        }
        if (outcome != null && playerName != null) {
            records.add(buildRecord(outcome));
        }
        return records;
    }

    // Factory method: TimeChallengeGame overrides it to build its own kind of record.
    protected GameRecord buildRecord(Outcome outcome) {
        return new SoloRecord(getId() + "-" + playerName, playerName, getDifficulty(), outcome, getFinalScore(),
                getElapsedSeconds(), mistakes, hintsUsed, getEndMillis());
    }
}
