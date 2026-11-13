package com.sudoclash.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Timer;

public class StyledButton extends JButton {
    public enum Style { PRIMARY, SECONDARY, DANGER, GHOST, TEXT, TEXT_DANGER, KEY, TAB, TILE }

    private final Style style;
    private Icons.Type icon;
    private Color accent;
    private String label;
    private boolean active;
    private boolean stretch;
    private boolean mouseFocus;
    private float hover;
    private int keySize = 54;
    private double scale = 1.0;
    private int keyFont = 24;
    private final Timer animation;

    public StyledButton(Icons.Type icon, String text, Style style) {
        this.style = style;
        this.icon = icon;
        this.label = text == null ? "" : text;
        super.setText(label);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setBorder(BorderFactory.createEmptyBorder());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        animation = new Timer(16, e -> {
            float target = getModel().isRollover() && isEnabled() ? 1f : 0f;
            hover += (target - hover) * 0.35f;
            if (Math.abs(target - hover) < 0.02f) {
                hover = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        getModel().addChangeListener(e -> {
            if (!animation.isRunning()) {
                animation.start();
            }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseFocus = true;
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                mouseFocus = false;
                repaint();
            }

            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }
        });
    }

    public void setLabel(Icons.Type icon, String text) {
        this.icon = icon;
        this.label = text == null ? "" : text;
        super.setText(label);
        revalidate();
        repaint();
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    // Number keys can grow with the window (see Widgets.NumberPad).
    public void setKeySize(int size) {
        if (size != keySize) {
            keySize = size;
            keyFont = Math.max(18, Math.round(size * 0.43f));
            revalidate();
            repaint();
        }
    }

    // Buttons of a side column grow a little with the window (1.0 = normal).
    public void setScale(double value) {
        double k = Math.round(Math.max(0.8, Math.min(2.0, value)) * 20) / 20.0;
        if (k != scale) {
            scale = k;
            revalidate();
            repaint();
        }
    }

    // The main colour of a PRIMARY button (default: the SudoClash teal) or the icon colour of the others.
    public StyledButton accent(Color color) {
        this.accent = color;
        repaint();
        return this;
    }

    public StyledButton stretch() {
        stretch = true;
        return this;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    private Font labelFont() {
        if (style == Style.TILE) {
            return Theme.bold((int) Math.round(12 * scale));
        }
        return style == Style.KEY ? Theme.plain(keyFont) : Theme.bold((int) Math.round(14 * scale));
    }

    @Override
    public Dimension getPreferredSize() {
        if (style == Style.KEY) {
            return new Dimension(keySize, keySize);
        }
        if (style == Style.TILE) {
            return new Dimension((int) Math.round(76 * scale), (int) Math.round(64 * scale));
        }
        FontMetrics fm = getFontMetrics(labelFont());
        int width = 40 + fm.stringWidth(label) + (icon == null ? 0 : 28);
        int height = style == Style.TEXT || style == Style.TEXT_DANGER ? 40 : 46;
        return new Dimension(Math.max(width, 110), (int) Math.round(height * scale));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = getPreferredSize();
        return stretch ? new Dimension(Integer.MAX_VALUE, d.height) : d;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.quality(g);
        int w = getWidth();
        int h = getHeight();
        boolean pressed = getModel().isPressed() && getModel().isArmed();
        boolean on = isEnabled();
        boolean solid = style == Style.PRIMARY || style == Style.DANGER;
        // One gentle corner shape for every button: a rounded rectangle, never a pill.
        int arc = style == Style.TAB ? h : 14;
        int press = pressed && on ? 1 : 0;

        Color bg;
        Color fg;
        Color border = null;
        Color main = accent != null ? accent : Theme.EMERALD;
        switch (style) {
            case PRIMARY:
                bg = Theme.blend(main, Theme.lighter(main, 0.25f), hover);
                if (pressed) {
                    bg = Theme.darker(main, 0.18f);
                }
                fg = Color.WHITE;
                break;
            case DANGER:
                bg = Theme.blend(Theme.ERROR, new Color(0xEF4444), hover);
                fg = Color.WHITE;
                break;
            case GHOST:
                bg = Theme.blend(new Color(255, 255, 255, 0), Theme.MINT, hover * 0.9f + (pressed ? 0.1f : 0f));
                fg = Theme.FOREST;
                break;
            case TEXT:
                bg = pressed ? Theme.MINT_MID : Theme.blend(new Color(221, 244, 242, 0), Theme.MINT, hover);
                fg = Theme.EMERALD_DARK;
                break;
            case TEXT_DANGER:
                bg = pressed ? new Color(0xFECACA) : Theme.blend(new Color(254, 226, 226, 0), Theme.ERROR_BG, hover);
                fg = Theme.ERROR;
                break;
            case TAB:
                // A pill: soft aqua when off, solid teal when it is the chosen one.
                if (active) {
                    bg = Theme.blend(Theme.EMERALD, Theme.EMERALD_HOVER, hover);
                    fg = Color.WHITE;
                } else {
                    bg = Theme.blend(Theme.MINT, Theme.MINT_MID, hover);
                    fg = Theme.FOREST;
                }
                break;
            case KEY:
                // A raised white number key with a thin aqua edge (turns mint on hover).
                bg = Theme.blend(Color.WHITE, Theme.MINT, hover * 0.9f + (pressed ? 0.1f : 0f));
                fg = Theme.FOREST;
                border = Theme.blend(new Color(0xBFE0DC), Theme.EMERALD, hover);
                break;
            case TILE:
                bg = Theme.blend(Color.WHITE, Theme.MINT_LIGHT, hover + (pressed ? 0.3f : 0f));
                fg = Theme.FOREST;
                border = Theme.blend(new Color(0xBFE0DC), Theme.EMERALD, hover);
                if (active) {
                    bg = Theme.MINT;
                    border = Theme.EMERALD;
                }
                break;
            default:
                bg = Theme.blend(Color.WHITE, Theme.MINT_LIGHT, hover + (pressed ? 0.3f : 0f));
                fg = Theme.FOREST;
                border = Theme.blend(Theme.BORDER, Theme.EMERALD, hover);
                if (active) {
                    // The "on" state (for example Pencil): a tinted tile with a teal edge.
                    bg = Theme.blend(Theme.MINT, Theme.MINT_MID, hover);
                    fg = Theme.FOREST;
                    border = Theme.EMERALD;
                }
                break;
        }
        if (!on) {
            bg = style == Style.TEXT || style == Style.TEXT_DANGER || style == Style.GHOST
                    ? new Color(0, 0, 0, 0) : Theme.DISABLED_BG;
            fg = Theme.DISABLED;
            border = null;
        }

        boolean flat = style == Style.TAB || style == Style.TEXT || style == Style.TEXT_DANGER || style == Style.GHOST;
        boolean raised = style == Style.KEY || style == Style.TILE;
        // Solid buttons and keys get a darker "lip" at the bottom, like the buttons in the reference artwork.
        int lip = on && (solid || raised) ? (style == Style.KEY || style == Style.TILE ? 3 : 3) : 0;
        if (on && !flat) {
            g.setColor(new Color(14, 95, 92, solid ? 40 : 18));
            g.fill(new RoundRectangle2D.Float(1, 2.5f + press, w - 3, h - 4, arc, arc));
        }
        if (lip > 0) {
            g.setColor(solid ? Theme.darker(bg, 0.28f) : new Color(0x9ACFC9));
            g.fill(new RoundRectangle2D.Float(1, 1 + press + lip, w - 3, h - 4 - lip + (flat ? 2 : 0), arc, arc));
        }
        RoundRectangle2D shape = new RoundRectangle2D.Float(1, 1 + press, w - 3, h - 4 - lip + (flat ? 2 : 0), arc, arc);
        if (solid && on) {
            g.setPaint(new java.awt.GradientPaint(0, 1, Theme.lighter(bg, 0.16f), 0, h - 4 - lip, bg));
        } else if (raised && on) {
            g.setPaint(new java.awt.GradientPaint(0, 1, bg, 0, h - 4 - lip, Theme.blend(bg, new Color(0xDDF0EE), 0.8f)));
        } else {
            g.setColor(bg);
        }
        g.fill(shape);
        if (border != null) {
            g.setColor(border);
            g.setStroke(new BasicStroke(active && style == Style.SECONDARY ? 1.8f : 1.2f));
            g.draw(shape);
        }
        if (isFocusOwner() && !mouseFocus && isFocusable()) {
            g.setColor(Theme.EMERALD);
            g.setStroke(new BasicStroke(2.2f));
            g.draw(new RoundRectangle2D.Float(3, 3, w - 7, h - 8, arc - 3, arc - 3));
        }
        int h2 = h - lip;

        // Icons are colourful on the light buttons (green tick, gold bulb, blue undo ...) and white on solid ones.
        Color iconInk = fg;
        if (on && (style == Style.SECONDARY || style == Style.TILE || style == Style.GHOST || style == Style.TEXT)) {
            iconInk = accent != null ? accent : Theme.iconColor(icon);
        }
        if (style == Style.TILE) {
            g.setFont(labelFont());
            FontMetrics tm = g.getFontMetrics();
            int is = (int) Math.round(24 * scale);
            int gap = 4;
            int blockH = is + gap + tm.getAscent();
            int top = (h2 - blockH) / 2 + press;
            if (icon != null) {
                Icons.paint(g, icon, (w - is) / 2.0, top, is, iconInk);
            }
            g.setColor(fg);
            g.drawString(label, (w - tm.stringWidth(label)) / 2, top + is + gap + tm.getAscent() - 1);
            g.dispose();
            return;
        }
        h = h2;
        g.setColor(fg);
        g.setFont(labelFont());
        FontMetrics fm = g.getFontMetrics();
        int iconSize = (int) Math.round(18 * scale);
        int iconSpace = icon == null ? 0 : iconSize + 9;
        int total = iconSpace + fm.stringWidth(label);
        int x = (w - total) / 2;
        if (icon != null) {
            Icons.paint(g, icon, x, (h - iconSize) / 2.0 + press, iconSize, iconInk);
        }
        g.drawString(label, x + iconSpace, (h + fm.getAscent() - fm.getDescent()) / 2 - 1 + press);
        g.dispose();
    }
}
