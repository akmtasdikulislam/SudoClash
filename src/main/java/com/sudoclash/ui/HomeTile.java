package com.sudoclash.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

// A large, colourful action tile of the dashboard: gradient face, white title, short description, a call-to-action
// pill and a ninja pose that fits the action. Painted at whatever size it is given; the art is never stretched.
public class HomeTile extends JComponent {
    private static final long serialVersionUID = 1L;

    private final Icons.Type icon;
    private final String title;
    private final String description;
    private final String cta;
    private final Color top;
    private final Color bottom;
    private final Art.Image art;
    private final Runnable action;
    private final boolean hero;

    private boolean inside;
    private boolean pressed;
    private boolean focus;
    private float hover;
    private final Timer animation;

    public HomeTile(Icons.Type icon, String title, String description, String cta, Color top, Color bottom,
                    Art.Image art, boolean hero, Runnable action) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.cta = cta;
        this.top = top;
        this.bottom = bottom;
        this.art = art;
        this.hero = hero;
        this.action = action;
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        animation = new Timer(16, e -> {
            float target = inside ? 1f : 0f;
            hover += (target - hover) * 0.3f;
            if (Math.abs(target - hover) < 0.02f) {
                hover = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                inside = true;
                animation.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                inside = false;
                pressed = false;
                animation.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                focus = false;
                requestFocusInWindow();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean fire = pressed && inside;
                pressed = false;
                repaint();
                if (fire) {
                    HomeTile.this.action.run();
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    HomeTile.this.action.run();
                }
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focus = !pressed;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focus = false;
                repaint();
            }
        });
    }

    public void click() {
        action.run();
    }

    @Override
    public Dimension getPreferredSize() {
        return hero ? new Dimension(520, 170) : new Dimension(250, 160);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(200, 110);
    }

    private static List<String> wrap(FontMetrics fm, String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String next = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(next) > width && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(next);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.quality(g);
        int w = getWidth() - 4;
        int h = getHeight() - 8;
        int lift = Math.round(hover * 3f);
        int x = 2;
        int y = 2 + (pressed ? 2 : 0) - (pressed ? 0 : lift);
        double k = Math.max(0.6, Math.min(1.7, h / (hero ? 170.0 : 160.0)));
        int arc = (int) Math.round(Math.min(26, 18 * Math.max(1.0, k * 0.9)));

        // ground shadow and the lip under the tile
        g.setColor(new Color(14, 60, 60, 26));
        g.fill(new RoundRectangle2D.Float(x, y + 6 + lift, w, h, arc, arc));
        g.setColor(Theme.darker(bottom, 0.22f));
        g.fill(new RoundRectangle2D.Float(x, y + 4, w, h, arc, arc));
        RoundRectangle2D face = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
        g.setPaint(new GradientPaint(0, y, Theme.lighter(top, hover * 0.08f), 0, y + h, bottom));
        g.fill(face);

        Shape oldClip = g.getClip();
        g.clip(face);
        // a soft glow and a faint grid in the corner keep the face from looking flat
        g.setPaint(new java.awt.RadialGradientPaint(x + w * 0.82f, y + h * 0.15f, Math.max(w, h) * 0.75f,
                new float[] {0f, 1f}, new Color[] {new Color(255, 255, 255, 70), new Color(255, 255, 255, 0)}));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(255, 255, 255, 20));
        g.setStroke(new BasicStroke(1f));
        int cell = Math.max(22, h / 5);
        for (int gx = x + w - cell * 5; gx < x + w; gx += cell) {
            g.drawLine(gx, y, gx, y + h);
        }
        for (int gy = y; gy < y + h; gy += cell) {
            g.drawLine(x + w - cell * 5, gy, x + w, gy);
        }
        // the ninja, anchored to the bottom right, never larger than the tile
        int artH = (int) Math.round(h * (hero ? 0.94 : 0.80));
        int artW = (int) Math.round(w * (hero ? 0.40 : 0.38));
        if (art != null) {
            Art.paintFit(g, art, x + w - artW - (int) (10 * k), y + h - artH - (int) (6 * k), artW, artH);
        }
        g.setClip(oldClip);

        // text
        int pad = (int) Math.round(18 * k);
        int textW = (int) Math.round(w * (art == null ? 0.9 : (hero ? 0.56 : 0.58))) - pad;
        int badge = (int) Math.round((hero ? 40 : 28) * k);
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new Ellipse2D.Float(x + pad, y + pad, badge, badge));
        int glyph = (int) Math.round(badge * 0.58);
        Icons.paint(g, icon, x + pad + (badge - glyph) / 2.0, y + pad + (badge - glyph) / 2.0, glyph, Color.WHITE);

        int ty = y + pad + badge + (int) Math.round(6 * k);
        float titleSize = (float) ((hero ? 30 : 21) * k);
        g.setFont(Theme.bold(1).deriveFont(titleSize));
        FontMetrics fm = g.getFontMetrics();
        while (fm.stringWidth(title) > textW - pad / 3 && titleSize > 12) {
            titleSize -= 1;
            g.setFont(Theme.bold(1).deriveFont(titleSize));
            fm = g.getFontMetrics();
        }
        g.setColor(new Color(0, 40, 40, 50));
        g.drawString(title, x + pad + 1, ty + fm.getAscent() + 1);
        g.setColor(Color.WHITE);
        g.drawString(title, x + pad, ty + fm.getAscent());
        ty += fm.getHeight();
        g.setFont(Theme.plain(1).deriveFont((float) ((hero ? 14.5 : 12) * k)));
        fm = g.getFontMetrics();
        g.setColor(new Color(255, 255, 255, 235));
        int shown = 0;
        for (String line : wrap(fm, description, textW)) {
            if (shown++ >= (hero ? 2 : 4)) {
                break;
            }
            g.drawString(line, x + pad, ty + fm.getAscent());
            ty += fm.getHeight() - 1;
        }

        // call to action pill at the bottom left
        g.setFont(Theme.bold(1).deriveFont((float) (13 * k)));
        fm = g.getFontMetrics();
        int pillH = (int) Math.round(30 * k);
        int pillW = fm.stringWidth(cta) + (int) Math.round(48 * k);
        int py = y + h - pad - pillH;
        if (py > ty) {
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(x + pad, py, pillW, pillH, pillH, pillH));
            g.setColor(Theme.darker(bottom, 0.1f));
            g.drawString(cta, x + pad + (int) Math.round(16 * k), py + (pillH + fm.getAscent() - fm.getDescent()) / 2);
            Icons.paint(g, Icons.Type.PLAY, x + pad + pillW - 14 * k - 12 * k * 0.6, py + pillH / 2.0 - 6 * k * 0.6,
                    (int) Math.round(12 * k * 0.6 + 6), Theme.darker(bottom, 0.1f));
        }
        if (focus) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.6f));
            g.draw(new RoundRectangle2D.Float(x + 4, y + 4, w - 8, h - 8, arc - 6, arc - 6));
            g.setColor(Theme.FOREST);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Float(x + 2, y + 2, w - 4, h - 4, arc - 2, arc - 2));
        }
        g.dispose();
    }
}
