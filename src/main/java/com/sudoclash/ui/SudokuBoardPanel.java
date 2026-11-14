package com.sudoclash.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

import com.sudoclash.game.BoardView;
import com.sudoclash.game.EmptyBoard;

public class SudokuBoardPanel extends JPanel {
    public interface BoardListener {
        void onCellSelected(int row, int col);

        void onNumber(int number);

        void onErase();

        void onTogglePencil();
    }

    private static final int MARGIN = 10;
    private static final Color BOX_TINT = new Color(0xF3FAF9);
    private static final Color LINE_THIN = new Color(0xC6DFDC);
    private static final Color SAME_NUMBER = new Color(0xB6E5E0);
    // Face-Off: who placed a number. Each player has a tint, a dark ink colour and a SHAPE (circle / square),
    // so the information does not depend on colour alone. Hints use amber and a small light bulb.
    public static final Color OWNER1_BG = new Color(0xDBEAFE);
    public static final Color OWNER1_INK = new Color(0x1D4ED8);
    public static final Color OWNER2_BG = new Color(0xFCE7F3);
    public static final Color OWNER2_INK = new Color(0xBE185D);
    public static final Color HINT_BG = new Color(0xFEF3C7);
    public static final Color HINT_INK = new Color(0xB45309);

    private BoardView view = new EmptyBoard();
    private BoardListener listener;
    private int selRow = -1;
    private int selCol = -1;
    private int hoverRow = -1;
    private int hoverCol = -1;
    // Small cell animations: a number "pops" when it is placed, a wrong number shakes. They only run for a
    // fraction of a second, and the timer stops by itself when nothing moves.
    private static final int POP_MILLIS = 320;
    private static final int SHAKE_MILLIS = 300;
    private final long[][] popStart = new long[9][9];
    private final long[][] shakeStart = new long[9][9];
    private final boolean[][] popIsHint = new boolean[9][9];
    private final javax.swing.Timer animationTimer = new javax.swing.Timer(16, e -> animationTick());
    private int hintRow = -1;
    private int hintCol = -1;

    public SudokuBoardPanel() {
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int[] cell = cellAt(e.getX(), e.getY());
                if (cell != null) {
                    select(cell[0], cell[1]);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                hoverCol = -1;
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int[] cell = cellAt(e.getX(), e.getY());
                int r = cell == null ? -1 : cell[0];
                int c = cell == null ? -1 : cell[1];
                if (r != hoverRow || c != hoverCol) {
                    hoverRow = r;
                    hoverCol = c;
                    repaint();
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e);
            }
        });
    }

    public void setBoardView(BoardView view) {
        this.view = view;
        clearSelection();
        clearHint();
    }

    public void setBoardListener(BoardListener listener) {
        this.listener = listener;
    }

    public int getSelectedRow() {
        return selRow;
    }

    public int getSelectedCol() {
        return selCol;
    }

    public boolean hasSelection() {
        return selRow >= 0;
    }

    public void select(int row, int col) {
        selRow = row;
        selCol = col;
        repaint();
        if (listener != null) {
            listener.onCellSelected(row, col);
        }
    }

    public void clearSelection() {
        selRow = -1;
        selCol = -1;
        repaint();
    }

    public void pop(int row, int col, boolean fromHint) {
        popStart[row][col] = System.currentTimeMillis();
        popIsHint[row][col] = fromHint;
        animationTimer.start();
        repaint();
    }

    public void shake(int row, int col) {
        shakeStart[row][col] = System.currentTimeMillis();
        animationTimer.start();
        repaint();
    }

    // True while any cell is still moving (used by the tests).
    public boolean isAnimating() {
        long now = System.currentTimeMillis();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (now - popStart[r][c] < POP_MILLIS && popStart[r][c] != 0
                        || now - shakeStart[r][c] < SHAKE_MILLIS && shakeStart[r][c] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void animationTick() {
        if (!isAnimating()) {
            animationTimer.stop();
        }
        repaint();
    }

    // 1.0 normally, up to about 1.4 for a moment right after a number was placed.
    private double popScale(int r, int c) {
        long age = System.currentTimeMillis() - popStart[r][c];
        if (popStart[r][c] == 0 || age >= POP_MILLIS) {
            return 1.0;
        }
        double x = age / (double) POP_MILLIS;
        return 1.0 + 0.38 * Math.sin(Math.PI * x) * (1 - x * 0.4);
    }

    private double shakeOffset(int r, int c, double cell) {
        long age = System.currentTimeMillis() - shakeStart[r][c];
        if (shakeStart[r][c] == 0 || age >= SHAKE_MILLIS) {
            return 0;
        }
        double x = age / (double) SHAKE_MILLIS;
        return Math.sin(x * Math.PI * 6) * (1 - x) * cell * 0.09;
    }

    public void showHint(int row, int col) {
        hintRow = row;
        hintCol = col;
        repaint();
    }

    public void clearHint() {
        hintRow = -1;
        hintCol = -1;
        repaint();
    }

    private void handleKey(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9) {
            fireNumber(code - KeyEvent.VK_0);
        } else if (code >= KeyEvent.VK_NUMPAD1 && code <= KeyEvent.VK_NUMPAD9) {
            fireNumber(code - KeyEvent.VK_NUMPAD0);
        } else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE
                || code == KeyEvent.VK_0 || code == KeyEvent.VK_NUMPAD0) {
            if (listener != null) {
                listener.onErase();
            }
        } else if (code == KeyEvent.VK_P) {
            if (listener != null) {
                listener.onTogglePencil();
            }
        } else if (code == KeyEvent.VK_LEFT) {
            moveSelection(0, -1);
        } else if (code == KeyEvent.VK_RIGHT) {
            moveSelection(0, 1);
        } else if (code == KeyEvent.VK_UP) {
            moveSelection(-1, 0);
        } else if (code == KeyEvent.VK_DOWN) {
            moveSelection(1, 0);
        }
    }

    private void fireNumber(int number) {
        if (listener != null) {
            listener.onNumber(number);
        }
    }

    private void moveSelection(int dRow, int dCol) {
        int r = selRow < 0 ? 4 : Math.max(0, Math.min(8, selRow + dRow));
        int c = selCol < 0 ? 4 : Math.max(0, Math.min(8, selCol + dCol));
        select(r, c);
    }

    private int boardSize() {
        return Math.max(9, Math.min(getWidth(), getHeight()) - 2 * MARGIN);
    }

    private int originX() {
        return (getWidth() - boardSize()) / 2;
    }

    private int originY() {
        return (getHeight() - boardSize()) / 2;
    }

    private int[] cellAt(int x, int y) {
        double cell = boardSize() / 9.0;
        int col = (int) Math.floor((x - originX()) / cell);
        int row = (int) Math.floor((y - originY()) / cell);
        if (row < 0 || row > 8 || col < 0 || col > 8) {
            return null;
        }
        return new int[]{row, col};
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(580, 580);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(320, 320);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.quality(g);
        int size = boardSize();
        int x0 = originX();
        int y0 = originY();
        double cell = size / 9.0;

        Theme.paintCard(g, x0, y0, size, size, 12, 8, Color.WHITE, null);
        java.awt.Shape clip = new RoundRectangle2D.Float(x0, y0, size, size, 12, 12);
        g.setClip(clip);

        int selValue = (selRow >= 0) ? view.getValue(selRow, selCol) : 0;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                // The tint of the owner (or hint) is the base; the other highlights are blended over it.
                int owner = view.getOwner(r, c);
                Color base = owner == 1 ? OWNER1_BG : owner == 2 ? OWNER2_BG : view.isHint(r, c) ? HINT_BG : null;
                Color bg = base;
                if (bg == null && ((r / 3 + c / 3) % 2 == 1)) {
                    bg = BOX_TINT;
                }
                boolean related = selRow >= 0 && (r == selRow || c == selCol
                        || (r / 3 == selRow / 3 && c / 3 == selCol / 3));
                if (related) {
                    bg = base == null ? Theme.MINT : Theme.blend(base, Theme.MINT, 0.45f);
                }
                if (selValue != 0 && view.getValue(r, c) == selValue) {
                    bg = base == null ? SAME_NUMBER : Theme.blend(base, SAME_NUMBER, 0.45f);
                }
                if (view.isWrong(r, c)) {
                    bg = Theme.PINK;
                }
                if (r == hoverRow && c == hoverCol) {
                    bg = bg == null ? Theme.MINT_LIGHT : Theme.blend(bg, Theme.MINT, 0.5f);
                }
                if (r == hintRow && c == hintCol) {
                    bg = Theme.WARN_BG;
                }
                if (r == selRow && c == selCol) {
                    // The chosen cell keeps its row/column tint here; its solid teal tile is drawn on top of the grid lines.
                    bg = view.isWrong(r, c) ? Theme.PINK : Theme.MINT;
                }
                if (bg != null) {
                    int cx = x0 + (int) Math.round(c * cell);
                    int cy = y0 + (int) Math.round(r * cell);
                    int cw = x0 + (int) Math.round((c + 1) * cell) - cx;
                    int ch = y0 + (int) Math.round((r + 1) * cell) - cy;
                    g.setColor(bg);
                    g.fillRect(cx, cy, cw, ch);
                }
            }
        }

        // Thin lines inside a box, thick lines between boxes.
        for (int i = 0; i <= 9; i++) {
            boolean thick = i % 3 == 0;
            g.setColor(thick ? Theme.GRID_BOX : LINE_THIN);
            g.setStroke(new BasicStroke(thick ? Math.max(2f, (float) (cell / 22)) : 1f));
            int pos = (int) Math.round(i * cell);
            g.drawLine(x0 + pos, y0, x0 + pos, y0 + size);
            g.drawLine(x0, y0 + pos, x0 + size, y0 + pos);
        }

        if (hintRow >= 0) {
            frame(g, x0, y0, cell, hintRow, hintCol, Theme.AMBER);
        }
        if (selRow >= 0 && !view.isWrong(selRow, selCol)) {
            // The chosen cell: a rounded solid teal tile (inset a little) with a soft shadow, number in white.
            double inset = Math.max(2.5, cell * 0.04);
            double sx = x0 + selCol * cell + inset;
            double sy = y0 + selRow * cell + inset;
            double sw = cell - 2 * inset;
            double arc = Math.max(8, cell * 0.2);
            g.setColor(new Color(10, 75, 70, 40));
            g.fill(new RoundRectangle2D.Double(sx, sy + 1.5, sw, sw, arc, arc));
            g.setColor(Theme.EMERALD);
            g.fill(new RoundRectangle2D.Double(sx, sy, sw, sw, arc, arc));
        }

        int numberSize = (int) Math.max(12, cell * 0.58);
        Font numberFont = Theme.plain(numberSize);
        Font userFont = Theme.bold(numberSize);
        Font markFont = Theme.bold((int) Math.max(8, cell * 0.24));
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                double left = x0 + c * cell;
                double top = y0 + r * cell;
                int value = view.getValue(r, c);
                if (value != 0) {
                    Color ink = Theme.EMERALD_DARK;
                    int owner = view.getOwner(r, c);
                    if (view.isGiven(r, c)) {
                        ink = Theme.TEXT;
                    } else if (view.isWrong(r, c)) {
                        ink = Theme.ERROR;
                    } else if (owner == 1) {
                        ink = OWNER1_INK;
                    } else if (owner == 2) {
                        ink = OWNER2_INK;
                    } else if (view.isHint(r, c)) {
                        ink = HINT_INK;
                    }
                    if (r == selRow && c == selCol && !view.isWrong(r, c)) {
                        ink = Color.WHITE;
                    }
                    g.setFont(view.isGiven(r, c) ? numberFont : userFont);
                    g.setColor(ink);
                    FontMetrics nm = g.getFontMetrics();
                    String text = String.valueOf(value);
                    double scale = popScale(r, c);
                    double shift = shakeOffset(r, c, cell);
                    if (scale != 1.0 && popIsHint[r][c]) {
                        // A short amber glow behind a number that a hint just filled in.
                        int alpha = (int) Math.max(0, 150 * (1 - (scale - 1.0) / 0.38 * 0.0) * (1.4 - scale));
                        g.setColor(new Color(245, 158, 11, Math.min(180, alpha)));
                        g.fill(new java.awt.geom.Ellipse2D.Double(left + cell * 0.1, top + cell * 0.1, cell * 0.8, cell * 0.8));
                        g.setColor(ink);
                    }
                    java.awt.geom.AffineTransform before = g.getTransform();
                    g.translate(left + cell / 2 + shift, top + cell / 2);
                    g.scale(scale, scale);
                    g.translate(-(left + cell / 2), -(top + cell / 2));
                    g.drawString(text, (int) Math.round(left + (cell - nm.stringWidth(text)) / 2),
                            (int) Math.round(top + (cell + nm.getAscent() - nm.getDescent()) / 2));
                    g.setTransform(before);
                    if (view.isWrong(r, c)) {
                        double s = Math.max(8, cell * 0.2);
                        Icons.paint(g, Icons.Type.CROSS, left + cell - s - 3, top + 3, s, Theme.ERROR);
                    } else if (owner == 1 || owner == 2) {
                        // A small circle (player 1) or square (player 2) in the corner.
                        double m = Math.max(5, cell * 0.14);
                        g.setColor(owner == 1 ? OWNER1_INK : OWNER2_INK);
                        if (owner == 1) {
                            g.fill(new java.awt.geom.Ellipse2D.Double(left + 4, top + 4, m, m));
                        } else {
                            g.fill(new java.awt.geom.Rectangle2D.Double(left + 4, top + 4, m, m));
                        }
                    } else if (view.isHint(r, c)) {
                        double s = Math.max(9, cell * 0.2);
                        Icons.paint(g, Icons.Type.HINT, left + 3, top + 3, s, HINT_INK);
                    }
                } else {
                    // Pencil marks are drawn in a 3x3 pattern.
                    g.setFont(markFont);
                    FontMetrics mm = g.getFontMetrics();
                    g.setColor(r == selRow && c == selCol ? Color.WHITE : Theme.MUTED);
                    for (int v = 1; v <= 9; v++) {
                        if (view.hasNote(r, c, v)) {
                            String text = String.valueOf(v);
                            double sx = left + cell * (((v - 1) % 3) + 0.5) / 3.0;
                            double sy = top + cell * (((v - 1) / 3) + 0.5) / 3.0;
                            g.drawString(text, (int) Math.round(sx - mm.stringWidth(text) / 2.0),
                                    (int) Math.round(sy + (mm.getAscent() - mm.getDescent()) / 2.0));
                        }
                    }
                }
            }
        }
        g.setClip(null);

        g.setColor(Theme.GRID_BOX);
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Float(x0 + 1f, y0 + 1f, size - 2, size - 2, 11, 11));
        if (isFocusOwner()) {
            g.setColor(Theme.alpha(Theme.EMERALD, 110));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(x0 - 4, y0 - 4, size + 8, size + 8, 16, 16));
        }
        g.dispose();
    }

    private void frame(Graphics2D g, int x0, int y0, double cell, int row, int col, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(3f));
        int cx = x0 + (int) Math.round(col * cell);
        int cy = y0 + (int) Math.round(row * cell);
        g.drawRect(cx + 1, cy + 1, (int) Math.round(cell) - 2, (int) Math.round(cell) - 2);
    }
}
