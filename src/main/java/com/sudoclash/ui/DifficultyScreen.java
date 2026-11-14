package com.sudoclash.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import com.sudoclash.game.GameTimer;
import com.sudoclash.sudoku.Difficulty;

public class DifficultyScreen extends Widgets.Backdrop {
    private static final long serialVersionUID = 1L;

    private final MainPanel app;
    private final java.util.List<ModeCard> cards = new java.util.ArrayList<>();
    private javax.swing.JLabel heading;
    private javax.swing.JLabel subheading;

    public DifficultyScreen(MainPanel app) {
        super(new GridBagLayout());
        this.app = app;
    }

    public void setMode(boolean timeChallenge) {
        removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.insets = new Insets(0, 0, 0, 0);
        add(new Art.Pic(timeChallenge ? Art.Image.THINK2 : Art.Image.THINK, 190, 170), c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 2, 0);
        heading = Theme.label(timeChallenge ? "Time Challenge" : "Solo", Theme.bold(32), Theme.FOREST);
        add(heading, c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 18, 0);
        subheading = Theme.label("Choose your difficulty", Theme.plain(17), Theme.MUTED);
        add(subheading, c);

        c.gridwidth = 1;
        c.gridy = 3;
        cards.clear();
        Icons.Type[] icons = {Icons.Type.LEVEL_1, Icons.Type.LEVEL_2, Icons.Type.LEVEL_3};
        Color[] colors = {Theme.EMERALD, Theme.AMBER, Theme.ERROR};
        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            final Difficulty level = levels[i];
            String extra = timeChallenge ? GameTimer.format(level.getTimeSeconds()) + " on the clock"
                    : level.getBaseScore() + " points";
            c.gridx = i;
            c.insets = new Insets(0, 8, 0, 8);
            ModeCard card = new ModeCard(icons[i], level.getLabel().toUpperCase(), level.getTagline() + "\n" + extra,
                    "PLAY", colors[i], true, () -> app.startSolo(level, timeChallenge));
            cards.add(card);
            add(card, c);
        }
        revalidate();
        repaint();
    }

    // The cards (and the heading) grow with the window, so a big screen is not left mostly empty.
    @Override
    public void doLayout() {
        double k = Math.max(0.9, Math.min(1.6, Math.min(getWidth() / 940.0, getHeight() / 640.0)));
        for (ModeCard card : cards) {
            card.setScale(k);
        }
        if (heading != null) {
            heading.setFont(Theme.bold((int) Math.round(32 * Math.min(k, 1.4))));
            subheading.setFont(Theme.plain((int) Math.round(17 * Math.min(k, 1.4))));
        }
        super.doLayout();
    }
}
