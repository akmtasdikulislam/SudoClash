package com.sudoclash.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import com.sudoclash.sudoku.Difficulty;

// The status pill above the board (time | score | level) and the small "Game info" card (mistakes, hints, progress).
// They only show numbers the game screen gives them.
public final class Hud {
    private Hud() {
    }

    public static class Pill extends JComponent {
        private static final long serialVersionUID = 1L;

        private String time = "00:00";
        private Color timeColor = Theme.TEXT;
        private boolean timeLeft;
        private String score = "0";
        private String level = "Easy";
        private int levelIndex;
        private double scale = 1.0;

        public Pill() {
            setOpaque(false);
        }

        public void setTime(String value) {
            time = value;
            repaint();
        }

        public void setTimeColor(Color color) {
            timeColor = color;
            repaint();
        }

        public void setTimeLeft(boolean value) {
            timeLeft = value;
            repaint();
        }

        public void setScore(String value) {
            score = value;
            repaint();
        }

        public void setLevel(Difficulty difficulty) {
            level = difficulty.getLabel();
            levelIndex = Math.min(2, difficulty.ordinal());
            repaint();
        }

        public void setScale(double value) {
            scale = Math.max(0.85, Math.min(2.0, value));
            revalidate();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) Math.round(560 * scale), (int) Math.round(78 * scale));
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(380, 60);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.quality(g);
            int pad = 8;
            int w = getWidth() - 2 * pad;
            int h = getHeight() - 2 * pad - 4;
            int y = pad;
            Theme.paintCard(g, pad, y, w, h, 40, 6, Color.WHITE, Theme.BORDER);
            int seg = w / 3;
            int mid = y + h / 2;
            double k = h / 54.0;
            int icon = (int) Math.round(26 * k);
            g.setStroke(new BasicStroke(1.2f));
            g.setColor(Theme.BORDER);
            g.drawLine(pad + seg, y + h / 5, pad + seg, y + h - h / 5);
            g.drawLine(pad + 2 * seg, y + h / 5, pad + 2 * seg, y + h - h / 5);

            g.setFont(Theme.bold((int) Math.round(21 * k)));
            FontMetrics fm = g.getFontMetrics();
            int base = mid + (fm.getAscent() - fm.getDescent()) / 2;

            // time
            String t = timeLeft ? time + " left" : time;
            int tw = icon + 10 + fm.stringWidth(t);
            int tx = pad + (seg - tw) / 2;
            Icons.paint(g, Icons.Type.CLOCK, tx, mid - icon / 2.0, icon, Theme.TEAL);
            g.setColor(timeColor);
            g.drawString(t, tx + icon + 10, base);

            // score with a gold star
            String sc = "Score: " + score;
            int sw = icon + 10 + fm.stringWidth(sc);
            int sx = pad + seg + (seg - sw) / 2;
            star(g, sx + icon / 2.0, mid, icon / 2.0);
            g.setColor(Theme.TEXT);
            g.drawString(sc, sx + icon + 10, base);

            // level
            int lw = icon + 10 + fm.stringWidth(level);
            int lx = pad + 2 * seg + (seg - lw) / 2;
            Icons.Type[] bars = {Icons.Type.LEVEL_1, Icons.Type.LEVEL_2, Icons.Type.LEVEL_3};
            Icons.paint(g, bars[levelIndex], lx, mid - icon / 2.0, icon, Theme.TEAL);
            g.setColor(Theme.TEXT);
            g.drawString(level, lx + icon + 10, base);
            g.dispose();
        }

        private static void star(Graphics2D g, double cx, double cy, double r) {
            Path2D p = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double ang = -Math.PI / 2 + i * Math.PI / 5;
                double rad = i % 2 == 0 ? r : r * 0.45;
                double x = cx + Math.cos(ang) * rad;
                double y = cy + Math.sin(ang) * rad + r * 0.06;
                if (i == 0) {
                    p.moveTo(x, y);
                } else {
                    p.lineTo(x, y);
                }
            }
            p.closePath();
            g.setPaint(new java.awt.GradientPaint(0, (float) (cy - r), new Color(0xFFD84D), 0, (float) (cy + r),
                    new Color(0xF2A91B)));
            g.fill(p);
            g.setColor(new Color(0xD98A0B));
            g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(p);
        }
    }

    public static class Info extends JComponent {
        private static final long serialVersionUID = 1L;

        private String mistakes = "0";
        private String hints = "0";
        private int progress;
        private double scale = 1.0;

        public Info() {
            setOpaque(false);
        }

        public void set(String mistakesValue, String hintsValue, int progressPercent) {
            mistakes = mistakesValue;
            hints = hintsValue;
            progress = Math.max(0, Math.min(100, progressPercent));
            repaint();
        }

        public void setScale(double value) {
            scale = Math.max(0.85, Math.min(2.0, value));
            revalidate();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, (int) Math.round(168 * scale));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.quality(g);
            int pad = 8;
            int w = getWidth() - 2 * pad;
            int h = getHeight() - 2 * pad;
            Theme.paintCard(g, pad, pad, w, h, 18, 5, Color.WHITE, Theme.BORDER);
            double k = scale;
            int x = pad + (int) (16 * k);
            int right = pad + w - (int) (16 * k);
            int icon = (int) Math.round(18 * k);

            g.setFont(Theme.bold((int) Math.round(13 * k)));
            g.setColor(Theme.MUTED);
            FontMetrics hm = g.getFontMetrics();
            int row = pad + (int) (28 * k);
            Icons.paint(g, Icons.Type.GRID, x, row - hm.getAscent() + 1, icon - 2, Theme.EMERALD_DARK);
            g.drawString("Game info", x + icon + 6, row);

            g.setFont(Theme.plain((int) Math.round(14 * k)));
            FontMetrics fm = g.getFontMetrics();
            int gap = (int) Math.round(32 * k);
            row += gap;
            line(g, fm, Icons.Type.CROSS, Theme.CORAL, "Mistakes", mistakes, x, right, row, icon, k);
            row += gap;
            line(g, fm, Icons.Type.HINT, Theme.GOLD, "Hints used", hints, x, right, row, icon, k);
            row += gap;
            line(g, fm, Icons.Type.CHECK, Theme.GREEN, "Progress", progress + "%", x, right, row, icon, k);
            // progress bar
            int by = row + (int) (8 * k);
            int bh = (int) Math.max(6, 8 * k);
            g.setColor(Theme.MINT);
            g.fill(new RoundRectangle2D.Float(x, by, right - x, bh, bh, bh));
            g.setPaint(new java.awt.GradientPaint(x, 0, Theme.EMERALD_HOVER, right, 0, Theme.GREEN));
            g.fill(new RoundRectangle2D.Float(x, by, Math.max(bh, (right - x) * progress / 100f), bh, bh, bh));
            g.dispose();
        }

        private static void line(Graphics2D g, FontMetrics fm, Icons.Type type, Color color, String label, String value,
                                 int x, int right, int baseline, int icon, double k) {
            Icons.paint(g, type, x, baseline - fm.getAscent() + 1, icon - 2, color);
            g.setColor(Theme.TEXT);
            g.drawString(label, x + icon + 6, baseline);
            g.setFont(Theme.bold((int) Math.round(15 * k)));
            FontMetrics bm = g.getFontMetrics();
            g.drawString(value, right - bm.stringWidth(value), baseline);
            g.setFont(Theme.plain((int) Math.round(14 * k)));
        }
    }

    // A big stopwatch with the score underneath. Solo: the hand sweeps once a minute and the ring fills with elapsed time
    // of the minute. Time Challenge: the ring shows how much of the time is left and turns amber, then red.
    public static class Watch extends JComponent {
        private static final long serialVersionUID = 1L;

        private String time = "00:00";
        private String score = "0";
        private String caption = "TIME";
        private String scoreCaption = "SCORE";
        private double fraction;
        private double seconds;
        private Color tone = Theme.EMERALD;
        private boolean countdown;
        private double scale = 1.0;

        public Watch() {
            setOpaque(false);
        }

        public void setScale(double value) {
            scale = Math.min(2.0, value);
            revalidate();
        }

        public void setScoreCaption(String value) {
            scoreCaption = value;
            repaint();
        }

        public void setScore(String value) {
            score = value;
            repaint();
        }

        // fraction: share of the ring that is filled (0..1); seconds: position of the thin hand.
        public void setTime(String text, double ringFraction, double handSeconds, Color color, boolean down) {
            time = text;
            fraction = Math.max(0, Math.min(1, ringFraction));
            seconds = handSeconds;
            tone = color;
            countdown = down;
            caption = down ? "TIME LEFT" : "TIME";
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(230, (int) Math.round(252 * scale));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.quality(g);
            int w = getWidth() - 4;
            int h = getHeight() - 6;
            Theme.paintCard(g, 0, 0, w, h, 18, 5, Color.WHITE, Theme.BORDER);
            int d = Math.min(w - 36, (int) (h * 0.5));
            int cx = w / 2;
            int cy = 22 + d / 2 + (int) (d * 0.06);
            // crown button of the stopwatch
            g.setColor(Theme.darker(tone, 0.2f));
            g.fillRoundRect(cx - d / 12, cy - d / 2 - d / 11, d / 6, d / 11, 5, 5);
            g.fillRoundRect(cx - d / 4, cy - d / 2 - d / 22, d / 2, d / 22, 5, 5);
            // case
            g.setPaint(new java.awt.GradientPaint(0, cy - d / 2f, Theme.lighter(tone, 0.25f), 0, cy + d / 2f, Theme.darker(tone, 0.15f)));
            g.fillOval(cx - d / 2, cy - d / 2, d, d);
            int in = Math.max(6, d / 14);
            g.setColor(Color.WHITE);
            g.fillOval(cx - d / 2 + in, cy - d / 2 + in, d - 2 * in, d - 2 * in);
            // ring that fills
            int rd = d - 2 * in - 14;
            g.setStroke(new java.awt.BasicStroke(Math.max(5f, d / 20f), java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.setColor(Theme.alpha(tone, 36));
            g.drawOval(cx - rd / 2, cy - rd / 2, rd, rd);
            g.setColor(tone);
            g.draw(new java.awt.geom.Arc2D.Double(cx - rd / 2.0, cy - rd / 2.0, rd, rd, 90, -360 * fraction, java.awt.geom.Arc2D.OPEN));
            // tick marks
            g.setStroke(new java.awt.BasicStroke(1.4f));
            g.setColor(Theme.alpha(Theme.MUTED, 120));
            int tr = rd / 2 - Math.max(8, d / 12);
            for (int i = 0; i < 12; i++) {
                double a = Math.PI * 2 * i / 12;
                int x1 = (int) (cx + Math.sin(a) * tr);
                int y1 = (int) (cy - Math.cos(a) * tr);
                int x2 = (int) (cx + Math.sin(a) * (tr - 5));
                int y2 = (int) (cy - Math.cos(a) * (tr - 5));
                g.drawLine(x1, y1, x2, y2);
            }
            // thin hand
            double ha = Math.PI * 2 * ((seconds % 60) / 60.0);
            g.setColor(Theme.alpha(Theme.CORAL, 200));
            g.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.drawLine(cx, cy, (int) (cx + Math.sin(ha) * (tr - 3)), (int) (cy - Math.cos(ha) * (tr - 3)));
            g.fillOval(cx - 3, cy - 3, 6, 6);
            // digital time in the middle of the face
            float size = Math.max(14f, d / 5.2f);
            g.setFont(Theme.bold(1).deriveFont(size));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(countdown && tone != Theme.EMERALD ? Theme.darker(tone, 0.15f) : Theme.FOREST);
            g.drawString(time, cx - fm.stringWidth(time) / 2, cy + d / 5 + fm.getAscent() / 2 - 4);
            g.setFont(Theme.bold(Math.max(9, d / 16)));
            fm = g.getFontMetrics();
            g.setColor(Theme.MUTED);
            g.drawString(caption, cx - fm.stringWidth(caption) / 2, cy - d / 5);

            // score underneath
            int bh = 58;
            int sy = h - 12 - bh;
            if (bh > 40) {
                g.setPaint(new java.awt.GradientPaint(0, sy, new Color(0xFFF4CF), 0, sy + bh, new Color(0xFFE29A)));
                g.fillRoundRect(16, sy, w - 32, bh, 16, 16);
                Icons.paint(g, Icons.Type.CROWN, 26, sy + (bh - 26) / 2.0, 26, new Color(0xE29A00));
                g.setFont(Theme.bold(10));
                fm = g.getFontMetrics();
                g.setColor(Theme.WARN_TEXT);
                g.drawString(scoreCaption, 62, sy + 4 + fm.getAscent());
                float big = 34f;
                g.setFont(Theme.bold(1).deriveFont(big));
                fm = g.getFontMetrics();
                g.setColor(new Color(0x7A4E00));
                g.drawString(score, 62, sy + bh - 5 - fm.getDescent());
            }
            g.dispose();
        }
    }
}
