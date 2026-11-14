package com.sudoclash.ui;

import javax.swing.SwingUtilities;

import com.sudoclash.game.ClientBoard;
import com.sudoclash.network.MatchStart;
import com.sudoclash.network.MessageType;
import com.sudoclash.network.NetworkMessage;

public abstract class MatchScreen extends BoardScreen {
    private static final long serialVersionUID = 1L;

    protected final MainPanel app;
    protected ClientBoard board;
    protected String me = "";

    protected MatchScreen(MainPanel app) {
        this(app, false);
    }

    // sidePad = true: the number keys form a keypad card on the right (like the solo screen) instead of a row below.
    protected MatchScreen(MainPanel app, boolean sidePad) {
        super(sidePad ? 250 : 264, sidePad, sidePad ? 268 : 276);
        this.app = app;
    }

    public void start(MatchStart info, String username) {
        me = username;
        board = new ClientBoard(info.getPuzzle());
        boardPanel.setBoardView(board);
        pencilMode = false;
        pencilButton.setActive(false);
        resetSidePanels(info);
        numberPad.refresh(board);
        SwingUtilities.invokeLater(boardPanel::requestFocusInWindow);
    }

    protected abstract void resetSidePanels(MatchStart info);

    public abstract void onMessage(NetworkMessage message);

    // The server decides whether the move is right.
    protected void sendMove(int row, int col, int value) {
        app.send(new NetworkMessage(MessageType.MOVE, row, col, value));
    }

    protected void refresh() {
        numberPad.refresh(board);
        boardPanel.repaint();
    }

    public void stop() {
        feedback.stop();
    }

    public void requestLeave() {
        boolean leave = Dialogs.confirm(this, Icons.Type.LOGOUT, "Leave match",
                "Leaving the match counts as a loss. Do you want to leave?", "LEAVE", "STAY", true);
        if (leave) {
            stop();
            app.leaveRoom();
        } else {
            boardPanel.requestFocusInWindow();
        }
    }
}
