package com.sudoclash.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sudoclash.game.CompeteProgress;
import com.sudoclash.game.Move;
import com.sudoclash.game.PlayerStatus;
import com.sudoclash.network.MatchStart;
import com.sudoclash.network.NetworkMessage;
import com.sudoclash.sudoku.Hint;
import com.sudoclash.sudoku.HintFinder;
import com.sudoclash.ui.Widgets.Feedback.Kind;

public class CompeteScreen extends MatchScreen {
    private static final long serialVersionUID = 1L;

    private boolean finished;

    private final Widgets.StatCard mistakesCard = new Widgets.StatCard("MISTAKES", Theme.ERROR, 30);
    private final Widgets.StatCard progressCard = new Widgets.StatCard("PROGRESS", Theme.EMERALD, 30);
    private final JPanel playerList = new JPanel();
    private final Hud.Watch watch = new Hud.Watch();
    private long startedAt;
    private final javax.swing.Timer matchClock = new javax.swing.Timer(500, e -> tickClock());
    private final Widgets.Progress myProgress = new Widgets.Progress(22);

    public CompeteScreen(MainPanel app) {
        super(app, true);
        playerList.setOpaque(false);
        playerList.setLayout(new BoxLayout(playerList, BoxLayout.Y_AXIS));

        // The big watch (match time + your progress) leads the column; the progress card's number lives in it.
        left.add(watch);
        left.add(Box.createVerticalStrut(10));
        left.add(mistakesCard);
        left.add(Box.createVerticalStrut(10));
        left.add(createPlayersCard());
        left.add(Box.createVerticalGlue());

        // Right: the same keypad card as the solo screen (number keys, pencil / undo / clear, hint).
        Widgets.Card padCard = new Widgets.Card(new BorderLayout(), 12) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        JLabel padTitle = Theme.label("Number Pad", Theme.bold(15), Theme.FOREST);
        padTitle.setIcon(Icons.icon(Icons.Type.GRID, 18, Theme.EMERALD_DARK));
        padTitle.setIconTextGap(8);
        padTitle.setAlignmentX(LEFT_ALIGNMENT);
        stack.add(padTitle);
        stack.add(Box.createVerticalStrut(10));
        numberPad.setAlignmentX(LEFT_ALIGNMENT);
        stack.add(centered(numberPad));
        stack.add(Box.createVerticalStrut(10));
        JPanel tools = new JPanel(new java.awt.GridLayout(1, 3, 8, 0)) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        tools.setOpaque(false);
        tools.setAlignmentX(LEFT_ALIGNMENT);
        tools.add(pencilButton);
        tools.add(createControl(Icons.Type.UNDO, "Undo", StyledButton.Style.TILE, "Undo last move", this::undo));
        tools.add(createControl(Icons.Type.CLEAR, "Clear", StyledButton.Style.TILE, "Clear selected cell",
                this::eraseSelected));
        stack.add(tools);
        stack.add(Box.createVerticalStrut(10));
        StyledButton hintButton = createControl(Icons.Type.HINT, "Hint", StyledButton.Style.PRIMARY,
                "Fill in a correct number (3 per puzzle)", this::hint);
        hintButton.setAlignmentX(LEFT_ALIGNMENT);
        stack.add(hintButton);
        padCard.add(stack, BorderLayout.CENTER);
        padCard.setAlignmentX(LEFT_ALIGNMENT);
        right.remove(0); // no glue above: the keypad starts level with the top of the board
        right.add(padCard);
        right.add(Box.createVerticalGlue());
    }

    private JComponent createPlayersCard() {
        Widgets.Card card = new Widgets.Card(new BorderLayout(0, 10), 14);
        JLabel title = new JLabel("PLAYERS", Icons.icon(Icons.Type.USERS, 20, Theme.EMERALD_DARK), JLabel.LEFT);
        title.setFont(Theme.section());
        title.setForeground(Theme.MUTED);
        title.setIconTextGap(8);
        card.add(title, BorderLayout.NORTH);
        card.add(playerList, BorderLayout.CENTER);
        JPanel mine = new JPanel(new BorderLayout(0, 4));
        mine.setOpaque(false);
        mine.add(Theme.label("Your progress", Theme.small(), Theme.MUTED), BorderLayout.NORTH);
        mine.add(myProgress, BorderLayout.CENTER);
        card.add(mine, BorderLayout.SOUTH);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        return card;
    }

    private void tickClock() {
        if (finished || !isShowing()) {
            matchClock.stop();
            return;
        }
        int seconds = (int) ((System.currentTimeMillis() - startedAt) / 1000);
        watch.setTime(com.sudoclash.game.GameTimer.format(seconds), (seconds % 60) / 60.0, seconds % 60, Theme.EMERALD, false);
    }

    @Override
    protected void resetSidePanels(MatchStart info) {
        startedAt = System.currentTimeMillis();
        watch.setScoreCaption("PROGRESS");
        watch.setScore("0%");
        watch.setTime("00:00", 0, 0, Theme.EMERALD, false);
        matchClock.restart();
        hintsUsed = 0;
        finished = false;
        mistakesCard.setValue("0");
        progressCard.setValue("0%");
        myProgress.setValue(0);

        Map<String, Integer> zero = new LinkedHashMap<>();
        Map<String, PlayerStatus> status = new LinkedHashMap<>();
        for (String player : info.getPlayers()) {
            zero.put(player, 0);
            status.put(player, PlayerStatus.PLAYING);
        }
        showProgress(new CompeteProgress(zero, zero, status, new ArrayList<String>()));
        feedback.show(Kind.INFO, "Race! The first player to fill the board wins.");
    }

    @Override
    public void onMessage(NetworkMessage m) {
        if (board == null) {
            return;
        }
        switch (m.getType()) {
            case MOVE_RESULT:

                board.markResult(m.getRow(), m.getCol(), m.getValue(), m.getFlag());
                if (m.getValue() != 0) {
                    feedback.show(m.getFlag() ? Kind.SUCCESS : Kind.ERROR,
                            m.getFlag() ? "Correct!" : "Incorrect value");
                    com.sudoclash.sound.Sounds.play(m.getFlag() ? com.sudoclash.sound.Sound.CORRECT
                            : com.sudoclash.sound.Sound.WRONG);
                }
                boardPanel.repaint();
                break;
            case PROGRESS:
                showProgress((CompeteProgress) m.getPayload());
                break;
            case PLAYER_FINISHED:
                if (m.getText().equals(me)) {
                    finished = true;
                    feedback.showSticky(Kind.SUCCESS, "You finished! Waiting for the others");
                } else {
                    feedback.show(Kind.INFO, m.getText() + " finished!");
                }
                break;
            case PLAYER_LEFT:
                feedback.show(Kind.WARNING, m.getText() + " left the match");
                break;
            case ERROR:
                feedback.show(Kind.WARNING, m.getText());
                break;
            default:
                break;
        }
    }

    private void showProgress(CompeteProgress info) {
        int mine = info.getPercent().getOrDefault(me, 0);
        myProgress.setValue(mine);
        progressCard.setValue(mine + "%");
        watch.setScore(mine + "%");
        mistakesCard.setValue(String.valueOf(info.getMistakes().getOrDefault(me, 0)));

        // Finished players first, then by progress, players who left last.
        List<String> order = new ArrayList<>(info.getFinishOrder());
        List<String> stillPlaying = new ArrayList<>();
        List<String> gone = new ArrayList<>();
        for (String name : info.getPercent().keySet()) {
            if (order.contains(name)) {
                continue;
            }
            if (info.getStatus().get(name) == PlayerStatus.LEFT) {
                gone.add(name);
            } else {
                stillPlaying.add(name);
            }
        }
        stillPlaying.sort((a, b) -> info.getPercent().get(b) - info.getPercent().get(a));
        order.addAll(stillPlaying);
        order.addAll(gone);

        playerList.removeAll();
        for (String name : order) {
            PlayerStatus state = info.getStatus().get(name);
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            Mark mark;
            if (state == PlayerStatus.FINISHED) {
                int place = info.getFinishOrder().indexOf(name) + 1;
                mark = new Mark(Theme.medalColor(place), String.valueOf(place));
            } else if (state == PlayerStatus.LEFT) {
                mark = new Mark(Theme.ERROR, "");
            } else {
                mark = new Mark(Theme.OK, "");
            }
            JLabel label = Theme.label(name + (name.equals(me) ? " (you)" : ""), Theme.bold(15),
                    state == PlayerStatus.LEFT ? Theme.MUTED : Theme.TEXT);
            JPanel nameRow = new JPanel(new BorderLayout(6, 0));
            nameRow.setOpaque(false);
            nameRow.add(mark, BorderLayout.WEST);
            nameRow.add(label, BorderLayout.CENTER);
            nameRow.setPreferredSize(new Dimension(128, 30));
            row.add(nameRow, BorderLayout.WEST);

            Widgets.Progress bar = new Widgets.Progress(28);
            bar.setValue(info.getPercent().get(name));
            bar.setFill(state == PlayerStatus.LEFT ? Theme.DISABLED
                    : state == PlayerStatus.FINISHED ? Theme.OK : Theme.EMERALD);
            row.add(bar, BorderLayout.CENTER);
            playerList.add(row);
        }
        playerList.revalidate();
        playerList.repaint();
    }

    @Override
    public void onNumber(int number) {
        if (board == null) {
            return;
        }
        int row = boardPanel.getSelectedRow();
        int col = boardPanel.getSelectedCol();
        if (row < 0) {
            feedback.show(Kind.WARNING, "Please select a cell first.");
            return;
        }
        if (!canAct() || board.isGiven(row, col)) {
            if (!finished) {
                feedback.show(Kind.WARNING, "Please select an editable cell.");
            }
            return;
        }
        if (pencilMode) {
            if (board.getValue(row, col) != 0) {
                feedback.show(Kind.WARNING, "Pencil marks go in empty cells.");
            } else {
                board.toggleNote(row, col, number);
            }
        } else if (board.getValue(row, col) != number) {
            // Show the number at once, then ask the server whether it is right.
            board.setValue(row, col, number);
            sendMove(row, col, number);
        }
        refresh();
    }

    @Override
    public void onErase() {
        eraseSelected();
    }

    private boolean canAct() {
        if (board == null) {
            return false;
        }
        if (finished) {
            feedback.show(Kind.INFO, "You already finished. Wait for the others.");
            return false;
        }
        return true;
    }

    private void eraseSelected() {
        if (!canAct()) {
            return;
        }
        int row = boardPanel.getSelectedRow();
        int col = boardPanel.getSelectedCol();
        if (row < 0) {
            feedback.show(Kind.WARNING, "Please select a cell first.");
            return;
        }
        if (board.isGiven(row, col)) {
            feedback.show(Kind.WARNING, "Please select an editable cell.");
            return;
        }
        if (board.getValue(row, col) != 0) {
            board.setValue(row, col, 0);
            sendMove(row, col, 0);
            feedback.show(Kind.INFO, "Cell cleared");
        } else {
            feedback.show(Kind.WARNING, "This cell is already empty.");
        }
        refresh();
    }

    private void undo() {
        if (!canAct()) {
            return;
        }
        Move move = board.undo();
        if (move == null) {
            feedback.show(Kind.WARNING, "Nothing to undo.");
            return;
        }
        // The server must know about the undo too.
        sendMove(move.getRow(), move.getCol(), move.getOldValue());
        feedback.show(Kind.INFO, "Move undone");
        refresh();
    }

    @Override
    protected boolean ninjaOnRight() {
        return true;
    }

    @Override
    protected Art.Image sceneNinja() {
        return Art.Image.SWORD;
    }

    private static final int MAX_HINTS = 3;
    private int hintsUsed;

    private void hint() {
        if (!canAct()) {
            return;
        }
        if (hintsUsed >= MAX_HINTS) {
            feedback.show(Kind.WARNING, "No hints left (" + MAX_HINTS + " per puzzle).");
            return;
        }
        Hint hint = new HintFinder(board.toCleanBoard()).findHint(3);
        if (hint == null) {
            feedback.show(Kind.WARNING, "No logical hint available right now.");
            return;
        }
        boardPanel.select(hint.getRow(), hint.getCol());
        boardPanel.showHint(hint.getRow(), hint.getCol());
        // The hint simply fills in the correct number: no dialog, no explanation of the technique.
        boolean apply = true;
        boardPanel.clearHint();
        if (apply && !finished) {
            hintsUsed++;
            board.setValue(hint.getRow(), hint.getCol(), hint.getValue());
            sendMove(hint.getRow(), hint.getCol(), hint.getValue());
        }
        refresh();
    }

    private static class Mark extends JComponent {
        private static final long serialVersionUID = 1L;

        private final Color color;
        private final String text;

        Mark(Color color, String text) {
            this.color = color;
            this.text = text;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(20, 20);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.quality(g);
            int y = (getHeight() - 18) / 2;
            g.setColor(color);
            if (text.isEmpty()) {
                g.fill(new Ellipse2D.Float(4, y + 4, 10, 10));
            } else {
                g.fill(new Ellipse2D.Float(0, y, 18, 18));
                g.setColor(Color.WHITE);
                g.setFont(Theme.bold(11));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(text, (18 - fm.stringWidth(text)) / 2, y + (18 + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }
}
