package com.sudoclash.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sudoclash.data.CompeteRecord;
import com.sudoclash.data.GameRecord;
import com.sudoclash.data.Outcome;
import com.sudoclash.exception.InvalidMoveException;
import com.sudoclash.sudoku.Puzzle;
import com.sudoclash.sudoku.SudokuBoard;

public class CompeteGame extends MultiplayerGame {
    // Encapsulation: the fields are private and used through getters and small methods.
    private static class PlayerBoard {
        private final SudokuBoard board;
        private int mistakes;
        private PlayerStatus status = PlayerStatus.PLAYING;

        PlayerBoard(SudokuBoard board) {
            this.board = board;
        }

        SudokuBoard getBoard() {
            return board;
        }

        int getMistakes() {
            return mistakes;
        }

        void addMistake() {
            mistakes++;
        }

        PlayerStatus getStatus() {
            return status;
        }

        void setStatus(PlayerStatus status) {
            this.status = status;
        }
    }

    private final Map<String, PlayerBoard> boards = new LinkedHashMap<>();
    private final SudokuBoard solution;
    private final List<String> finishOrder = new ArrayList<>();
    private final List<String> leaveOrder = new ArrayList<>();
    private final Map<String, Integer> finishSeconds = new LinkedHashMap<>();
    private boolean over;
    private String winner;
    private String reason = "";

    public CompeteGame(Puzzle puzzle, List<String> players) {
        super(puzzle, players);
        this.solution = puzzle.getSolution();
        for (String player : players) {
            boards.put(player, new PlayerBoard(puzzle.getBoard()));
        }
        start();
    }

    @Override
    public MatchMode getMode() {
        return MatchMode.COMPETE;
    }

    public boolean makeMove(String player, int row, int col, int value) throws InvalidMoveException {
        if (over) {
            throw new InvalidMoveException("The match is over.");
        }
        PlayerBoard playerBoard = boards.get(player);
        if (playerBoard == null || playerBoard.getStatus() != PlayerStatus.PLAYING) {
            throw new InvalidMoveException("You are not playing in this match.");
        }
        if (row < 0 || row > 8 || col < 0 || col > 8 || value < 0 || value > 9) {
            throw new InvalidMoveException("Invalid move.");
        }
        if (getPuzzle().isGiven(row, col)) {
            throw new InvalidMoveException("Starting numbers cannot be changed.");
        }

        boolean correct = (value == 0) || (value == solution.getValue(row, col));
        if (playerBoard.getBoard().getValue(row, col) == value) {
            return correct;
        }
        playerBoard.getBoard().setValue(row, col, value);
        if (!correct) {
            playerBoard.addMistake();
        }
        if (playerBoard.getBoard().sameAs(solution)) {
            playerBoard.setStatus(PlayerStatus.FINISHED);
            finishOrder.add(player);
            finishSeconds.put(player, getElapsedSeconds());
            checkEnd();
        }
        return correct;
    }

    @Override
    public void playerLeft(String player) {
        PlayerBoard playerBoard = boards.get(player);
        if (playerBoard == null || over || playerBoard.getStatus() != PlayerStatus.PLAYING) {
            return;
        }
        playerBoard.setStatus(PlayerStatus.LEFT);
        leaveOrder.add(player);
        checkEnd();
    }

    // The match ends when nobody is playing, or when the last player is left and nobody finished.
    private void checkEnd() {
        List<String> stillPlaying = new ArrayList<>();
        for (String name : boards.keySet()) {
            if (boards.get(name).getStatus() == PlayerStatus.PLAYING) {
                stillPlaying.add(name);
            }
        }
        if (stillPlaying.isEmpty()) {
            winner = finishOrder.isEmpty() ? null : finishOrder.get(0);
            reason = finishOrder.isEmpty() ? "Everyone left. There is no winner." : "Match complete.";
            endMatch();
        } else if (finishOrder.isEmpty() && stillPlaying.size() == 1 && !leaveOrder.isEmpty()) {
            winner = stillPlaying.get(0);
            reason = "All other players left. " + winner + " wins.";
            endMatch();
        }
    }

    private void endMatch() {
        over = true;
        finish(GameState.COMPLETED);
    }

    public boolean hasFinished(String player) {
        return finishOrder.contains(player);
    }

    public int getPercent(String player) {
        PlayerBoard playerBoard = boards.get(player);
        int total = 81 - getPuzzle().getClueCount();
        if (playerBoard == null || total == 0) {
            return 0;
        }
        int done = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (!getPuzzle().isGiven(row, col)
                        && playerBoard.getBoard().getValue(row, col) == solution.getValue(row, col)) {
                    done++;
                }
            }
        }
        return done * 100 / total;
    }

    public CompeteProgress getProgress() {
        Map<String, Integer> percent = new LinkedHashMap<>();
        Map<String, Integer> mistakes = new LinkedHashMap<>();
        Map<String, PlayerStatus> status = new LinkedHashMap<>();
        for (String name : boards.keySet()) {
            percent.put(name, getPercent(name));
            mistakes.put(name, boards.get(name).getMistakes());
            status.put(name, boards.get(name).getStatus());
        }
        return new CompeteProgress(percent, mistakes, status, finishOrder);
    }

    @Override
    public boolean isOver() {
        return over;
    }

    // Ranking: finishers, then a winner by forfeit, then players who left (later leavers rank higher).
    private List<String> buildStandings() {
        List<String> standings = new ArrayList<>(finishOrder);
        if (winner != null && !standings.contains(winner)) {
            standings.add(winner);
        }
        List<String> leavers = new ArrayList<>(leaveOrder);
        Collections.reverse(leavers);
        for (String name : leavers) {
            if (!standings.contains(name)) {
                standings.add(name);
            }
        }
        return standings;
    }

    @Override
    public MatchResult getResult() {
        List<String> standings = buildStandings();
        Map<String, String> details = new LinkedHashMap<>();
        for (String name : standings) {
            PlayerBoard playerBoard = boards.get(name);
            int mistakes = playerBoard.getMistakes();
            String text = mistakes + " mistake" + (mistakes == 1 ? "" : "s");
            if (playerBoard.getStatus() == PlayerStatus.LEFT) {
                text = "left the match, " + text;
            } else if (playerBoard.getStatus() == PlayerStatus.FINISHED) {
                text = "finished, " + text;
            }
            details.put(name, text);
        }
        return new MatchResult(MatchMode.COMPETE, standings, winner, reason, details);
    }

    // Runtime polymorphism: the server calls createRecords() on any MultiplayerGame.
    @Override
    public List<GameRecord> createRecords() {
        List<GameRecord> records = new ArrayList<>();
        if (!over || winner == null) {
            return records;
        }
        List<String> standings = buildStandings();
        for (int i = 0; i < standings.size(); i++) {
            String name = standings.get(i);
            Outcome outcome = name.equals(winner) ? Outcome.WIN : Outcome.LOSS;
            records.add(new CompeteRecord(getId() + "-" + name, name, getDifficulty(), outcome,
                    finishSeconds.getOrDefault(name, 0), boards.get(name).getMistakes(), i + 1, standings.size(),
                    getEndMillis()));
        }
        return records;
    }
}
