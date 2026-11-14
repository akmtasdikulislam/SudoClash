package com.sudoclash.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

// The painted backgrounds. Everything is drawn with vector shapes (layered hills, mist, pagoda, torii, cherry blossom,
// moon), so it is sharp at any window size. All colours stay inside the SudoClash palette (white, aqua, mint, teal):
//  - soft:        the quiet page background of the other screens;
//  - lobby:       the most atmospheric scene: moon, pagoda, torii, blossoms around the two ninjas;
//  - multiplayer: a bright morning version (sun, big torii, blossom branch) for the entry page;
//  - game:        the calmest one; detail only in the lower corners, a soft spotlight behind the board.
public final class Scenery {
    private Scenery() {
    }

    private static BufferedImage canvas(int w, int h) {
        return new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_RGB);
    }

    private static Graphics2D open(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        Theme.quality(g);
        return g;
    }


    private static Color c(int rgb, int alpha) {
        return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, alpha);
    }

    // ------------------------------------------------------------------------------------------ building blocks

    // A line of hills or mountains across the window.
    private static final class Ridge {
        final int w;
        final int h;
        final double baseY;
        final double amp;
        final double seed;
        final double freq;
        final boolean peaks;

        Ridge(int w, int h, double baseY, double amp, double seed, double freq, boolean peaks) {
            this.w = w;
            this.h = h;
            this.baseY = baseY;
            this.amp = amp;
            this.seed = seed;
            this.freq = freq;
            this.peaks = peaks;
        }

        double y(double x) {
            double a = Math.sin(x * freq + seed);
            double v = peaks ? 0.55 * (1 - Math.abs(a)) + 0.30 * Math.sin(x * freq * 2.3 + seed * 1.7) * 0.5 + 0.15
                    : 0.5 + 0.5 * (0.6 * a + 0.3 * Math.sin(x * freq * 2.3 + seed * 1.7) + 0.1 * Math.sin(x * freq * 5.1));
            return baseY - amp * Math.max(0, Math.min(1, v));
        }

        void paint(Graphics2D g, Color top, Color bottom) {
            Path2D p = new Path2D.Double();
            p.moveTo(-10, h + 10);
            int step = Math.max(3, w / 240);
            for (int x = -10; x <= w + 10 + step; x += step) {
                p.lineTo(x, y(x));
            }
            p.lineTo(w + 10 + step, h + 10);
            p.closePath();
            g.setPaint(new GradientPaint(0, (float) (baseY - amp), top, 0, (float) Math.min(h, baseY + amp * 1.4), bottom));
            g.fill(p);
        }
    }

    private static void mist(Graphics2D g, int w, double y, double height, int alpha) {
        g.setPaint(new GradientPaint(0, (float) y, new Color(255, 255, 255, 0), 0, (float) (y + height * 0.5),
                new Color(255, 255, 255, alpha)));
        g.fill(new java.awt.geom.Rectangle2D.Double(0, y, w, height * 0.5));
        g.setPaint(new GradientPaint(0, (float) (y + height * 0.5), new Color(255, 255, 255, alpha), 0,
                (float) (y + height), new Color(255, 255, 255, 0)));
        g.fill(new java.awt.geom.Rectangle2D.Double(0, y + height * 0.5, w, height * 0.5));
    }

    private static void cloud(Graphics2D g, double cx, double cy, double size, int alpha) {
        g.setColor(new Color(255, 255, 255, alpha));
        g.fill(new Ellipse2D.Double(cx - size * 0.5, cy - size * 0.10, size, size * 0.24));
        g.fill(new Ellipse2D.Double(cx - size * 0.28, cy - size * 0.22, size * 0.42, size * 0.30));
        g.fill(new Ellipse2D.Double(cx - size * 0.02, cy - size * 0.17, size * 0.36, size * 0.26));
    }

    private static void moon(Graphics2D g, double cx, double cy, double r, int alpha) {
        g.setPaint(new RadialGradientPaint((float) cx, (float) cy, (float) (r * 3.4), new float[] {0f, 0.35f, 1f},
                new Color[] {new Color(255, 255, 255, alpha), new Color(255, 255, 255, alpha / 3), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(cx - r * 3.4, cy - r * 3.4, r * 6.8, r * 6.8));
        g.setColor(new Color(255, 255, 255, Math.min(255, alpha + 30)));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
    }

    // A five-storey pagoda silhouette standing on the point (cx, baseY).
    private static void pagoda(Graphics2D g, double cx, double baseY, double size, Color color) {
        g.setColor(color);
        double tiers = 5;
        double th = size / (tiers + 1.2);
        double y = baseY;
        for (int i = 0; i < tiers; i++) {
            double rw = size * (0.36 - i * 0.055);
            double bw = rw * 0.62;
            // body
            g.fill(new java.awt.geom.Rectangle2D.Double(cx - bw, y - th * 0.78, bw * 2, th * 0.78));
            // roof with upswept corners
            Path2D roof = new Path2D.Double();
            roof.moveTo(cx - rw * 1.12, y - th * 0.70);
            roof.quadTo(cx - rw * 0.55, y - th * 0.62, cx - rw * 0.30, y - th * 1.08);
            roof.lineTo(cx + rw * 0.30, y - th * 1.08);
            roof.quadTo(cx + rw * 0.55, y - th * 0.62, cx + rw * 1.12, y - th * 0.70);
            roof.quadTo(cx, y - th * 0.52, cx - rw * 1.12, y - th * 0.70);
            roof.closePath();
            g.fill(roof);
            y -= th * 0.98;
        }
        g.setStroke(new BasicStroke((float) Math.max(1.5, size * 0.012)));
        g.draw(new java.awt.geom.Line2D.Double(cx, y - th * 0.1, cx, y - th * 1.2));
    }

    private static void torii(Graphics2D g, double cx, double baseY, double size, Color color) {
        double post = size * 0.075;
        double half = size * 0.36;
        g.setColor(color);
        g.fill(new java.awt.geom.Rectangle2D.Double(cx - half, baseY - size, post, size));
        g.fill(new java.awt.geom.Rectangle2D.Double(cx + half - post, baseY - size, post, size));
        // lower beam
        g.fill(new java.awt.geom.Rectangle2D.Double(cx - half - post * 0.2, baseY - size * 0.72, half * 2 + post * 0.4, post * 0.8));
        // upper beam: slightly curved, ends turn up
        Path2D top = new Path2D.Double();
        double ty = baseY - size;
        top.moveTo(cx - half - size * 0.17, ty - size * 0.03);
        top.quadTo(cx, ty + size * 0.07, cx + half + size * 0.17, ty - size * 0.03);
        top.lineTo(cx + half + size * 0.13, ty + size * 0.045);
        top.quadTo(cx, ty + size * 0.12, cx - half - size * 0.13, ty + size * 0.045);
        top.closePath();
        g.fill(top);
    }

    private static void blossom(Graphics2D g, double x, double y, double r, double rot, int alpha) {
        java.awt.geom.AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(rot);
        g.setColor(new Color(255, 232, 238, alpha));
        for (int k = 0; k < 5; k++) {
            g.rotate(Math.toRadians(72));
            g.fill(new Ellipse2D.Double(-r * 0.32, -r * 0.98, r * 0.64, r * 0.98));
        }
        g.setColor(new Color(255, 190, 150, Math.min(255, alpha + 20)));
        g.fill(new Ellipse2D.Double(-r * 0.14, -r * 0.14, r * 0.28, r * 0.28));
        g.setTransform(old);
    }

    // A cherry-blossom branch growing from (x0,y0) towards (x1,y1), bending through (cx,cy).
    private static void branch(Graphics2D g, double x0, double y0, double cx, double cy, double x1, double y1,
                               double thick, long seed, int alpha) {
        java.util.Random rnd = new java.util.Random(seed);
        java.awt.geom.QuadCurve2D.Double curve = new java.awt.geom.QuadCurve2D.Double(x0, y0, cx, cy, x1, y1);
        g.setColor(c(0x3F7F7B, Math.min(255, alpha)));
        g.setStroke(new BasicStroke((float) thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(curve);
        double len = Math.hypot(x1 - x0, y1 - y0);
        int n = (int) Math.max(5, len / (thick * 5.2));
        for (int i = 1; i <= n; i++) {
            double t = 0.18 + 0.82 * i / n;
            double mt = 1 - t;
            double px = mt * mt * x0 + 2 * mt * t * cx + t * t * x1;
            double py = mt * mt * y0 + 2 * mt * t * cy + t * t * y1;
            // a short twig with a blossom (or two) at its end
            double ang = rnd.nextDouble() * Math.PI * 2;
            double tl = thick * (1.8 + rnd.nextDouble() * 3.2);
            double ex = px + Math.cos(ang) * tl;
            double ey = py + Math.sin(ang) * tl;
            g.setStroke(new BasicStroke((float) Math.max(1.2, thick * 0.32), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c(0x3F7F7B, Math.min(255, alpha)));
            g.draw(new java.awt.geom.Line2D.Double(px, py, ex, ey));
            double r = thick * (1.3 + rnd.nextDouble() * 0.9);
            blossom(g, ex, ey, r, rnd.nextDouble() * 6.28, alpha);
            if (rnd.nextInt(3) == 0) {
                blossom(g, px + (ex - px) * 0.4, py + (ey - py) * 0.4 + r * 0.5, r * 0.8, rnd.nextDouble() * 6.28, alpha);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private static void petals(Graphics2D g, int count, long seed, double x, double y, double w, double h, double size,
                               int alpha) {
        java.util.Random rnd = new java.util.Random(seed);
        for (int i = 0; i < count; i++) {
            double px = x + rnd.nextDouble() * w;
            double py = y + rnd.nextDouble() * h;
            double s = size * (0.6 + rnd.nextDouble() * 0.8);
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(px, py);
            g.rotate(rnd.nextDouble() * 6.28);
            g.setColor(new Color(255, 230, 237, alpha));
            g.fill(new Ellipse2D.Double(-s, -s * 0.55, s * 2, s * 1.1));
            g.setTransform(old);
        }
    }

    private static void sky(Graphics2D g, int w, int h, int top, int mid, int bottom) {
        g.setPaint(new GradientPaint(0, 0, c(top, 255), 0, h * 0.62f, c(mid, 255)));
        g.fillRect(0, 0, w, (int) (h * 0.62f) + 1);
        g.setPaint(new GradientPaint(0, h * 0.62f, c(mid, 255), 0, h, c(bottom, 255)));
        g.fillRect(0, (int) (h * 0.62f), w, h);
    }


    // A bamboo/leaf sprig: a gently bent stem with pointed leaves on both sides.
    private static void leafSprig(Graphics2D g, double x0, double y0, double cx, double cy, double x1, double y1,
                                  double leaf, int count, long seed, Color color) {
        java.util.Random rnd = new java.util.Random(seed);
        g.setColor(color);
        g.setStroke(new BasicStroke((float) Math.max(1.5, leaf * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.QuadCurve2D.Double(x0, y0, cx, cy, x1, y1));
        for (int i = 1; i <= count; i++) {
            double t = (double) i / (count + 1);
            double mt = 1 - t;
            double px = mt * mt * x0 + 2 * mt * t * cx + t * t * x1;
            double py = mt * mt * y0 + 2 * mt * t * cy + t * t * y1;
            double tx = 2 * mt * (cx - x0) + 2 * t * (x1 - cx);
            double ty = 2 * mt * (cy - y0) + 2 * t * (y1 - cy);
            double base = Math.atan2(ty, tx);
            for (int side = -1; side <= 1; side += 2) {
                double ang = base + side * (0.7 + rnd.nextDouble() * 0.5);
                double len = leaf * (0.8 + rnd.nextDouble() * 0.5);
                java.awt.geom.AffineTransform old = g.getTransform();
                g.translate(px, py);
                g.rotate(ang);
                Path2D lf = new Path2D.Double();
                lf.moveTo(0, 0);
                lf.quadTo(len * 0.5, -len * 0.20, len, 0);
                lf.quadTo(len * 0.5, len * 0.20, 0, 0);
                g.fill(lf);
                g.setTransform(old);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    // The shared Japanese-mist scene of the quiet screens and the game: sun glow, far mountains with pagodas, mist,
    // soft hills, leaf sprigs in the corners and a few floating Sudoku tiles. strength 0..1 scales every alpha.
    private static void scenic(Graphics2D g, int w, int h, double strength, double pagodaSize) {
        scenic(g, w, h, strength, pagodaSize, true);
    }

    private static void scenic(Graphics2D g, int w, int h, double strength, double pagodaSize, boolean tiles) {
        int m = Math.min(w, h);
        sky(g, w, h, 0xD9F1EE, 0xEAF8F6, 0xF5FCFC);
        int a = (int) (255 * strength);
        // sun glow in the upper left
        float gr = (float) (m * 0.55);
        g.setPaint(new RadialGradientPaint(w * 0.20f, h * 0.17f, gr, new float[] {0f, 1f},
                new Color[] {new Color(255, 255, 255, (int) (200 * Math.min(1, strength + 0.2))), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Float(w * 0.20f - gr, h * 0.17f - gr, gr * 2, gr * 2));
        moon(g, w * 0.20, h * 0.17, m * 0.045, (int) (120 * strength));

        Ridge far = new Ridge(w, h, h * 0.74, h * 0.24, 1.9, 0.0049, true);
        far.paint(g, c(0x9BD9D2, (int) (a * 0.55)), c(0xCBEDEA, (int) (a * 0.25)));
        double lx = w * 0.115;
        pagoda(g, lx, far.y(lx) + h * 0.012, pagodaSize, c(0x4FAFA8, (int) (a * 0.40)));
        double rx = w * 0.90;
        pagoda(g, rx, far.y(rx) + h * 0.012, pagodaSize * 0.62, c(0x4FAFA8, (int) (a * 0.30)));
        double mx = w * 0.20;
        pagoda(g, mx, far.y(mx) + h * 0.012, pagodaSize * 0.36, c(0x4FAFA8, (int) (a * 0.25)));
        mist(g, w, h * 0.58, h * 0.20, (int) (a * 0.6));
        Ridge mid = new Ridge(w, h, h * 0.87, h * 0.12, 4.4, 0.0046, false);
        mid.paint(g, c(0x86D0C9, (int) (a * 0.55)), c(0xB3E2DD, (int) (a * 0.40)));
        mist(g, w, h * 0.78, h * 0.13, (int) (a * 0.45));
        Ridge front = new Ridge(w, h, h * 0.97, h * 0.06, 0.4, 0.0043, false);
        front.paint(g, c(0x76C8C1, (int) (a * 0.55)), c(0xA1DAD5, (int) (a * 0.45)));

        // leaf sprigs in the corners (very light)
        Color leaf = c(0x3FA79F, (int) (a * 0.34));
        leafSprig(g, -8, -8, w * 0.07, h * 0.03, w * 0.15, h * 0.16, m * 0.045, 7, 3, leaf);
        leafSprig(g, -8, h * 0.05, w * 0.03, h * 0.12, w * 0.07, h * 0.28, m * 0.04, 5, 8, leaf);
        leafSprig(g, w + 8, -8, w * 0.93, h * 0.04, w * 0.86, h * 0.15, m * 0.045, 7, 5, leaf);
        leafSprig(g, w + 8, h * 0.06, w * 0.97, h * 0.13, w * 0.93, h * 0.27, m * 0.04, 5, 9, leaf);
        // a few petals and floating tiles
        petals(g, 8, 33, w * 0.02, h * 0.05, w * 0.96, h * 0.8, m * 0.008, (int) (170 * strength));
        int tile = Math.max(26, (int) (m * 0.055));
        int[][] spots = {
            {(int) (w * 0.055), (int) (h * 0.33), 5, -9},
            {(int) (w * 0.935 - tile), (int) (h * 0.42), 9, 8},
            {(int) (w * 0.17), (int) (h * 0.07), 3, 6},
        };
        for (int[] sp : spots) {
            if (tiles) {
                tileAt(g, sp[0], sp[1], tile, sp[2], sp[3]);
            }
        }
    }

    // ----------------------------------------------------------------------------------------------- page (soft)

    // The quiet page background used by the screens that have no scene of their own.
    public static BufferedImage soft(int w, int h) {
        BufferedImage image = canvas(w, h);
        Graphics2D g = open(image);
        scenic(g, w, h, 0.62, Math.min(w, h) * 0.30);
        g.dispose();
        return image;
    }

    // ------------------------------------------------------------------------------------------------------ lobby

    // Where the ninjas' feet are in the lobby scene (the stage uses the same line).
    public static int lobbyGround(int h) {
        return (int) (h * 0.84);
    }

    // stageX: where the artwork area starts (the left part holds the room controls and stays calm).
    public static BufferedImage lobby(int w, int h, int stageX) {
        BufferedImage image = canvas(w, h);
        Graphics2D g = open(image);
        int m = Math.min(w, h);
        double sw = Math.max(200, w - stageX);
        double scx = stageX + sw * 0.5;
        sky(g, w, h, 0xB9E7E2, 0xDDF3F0, 0xF2FBFA);

        // The moon behind the two ninjas, with soft rings.
        double mr = Math.min(sw, h) * 0.15;
        moon(g, scx, h * 0.30, mr, 215);
        g.setStroke(new BasicStroke(Math.max(1.5f, m / 420f)));
        g.setColor(new Color(255, 255, 255, 120));
        g.draw(new Ellipse2D.Double(scx - mr * 1.55, h * 0.30 - mr * 1.55, mr * 3.1, mr * 3.1));
        g.setColor(new Color(255, 255, 255, 60));
        g.draw(new Ellipse2D.Double(scx - mr * 2.2, h * 0.30 - mr * 2.2, mr * 4.4, mr * 4.4));

        cloud(g, stageX + sw * 0.16, h * 0.22, sw * 0.26, 150);
        cloud(g, stageX + sw * 0.84, h * 0.36, sw * 0.22, 120);

        // Far mountains and a pagoda on the right peak.
        Ridge far = new Ridge(w, h, h * 0.66, h * 0.26, 2.2, 0.0058, true);
        far.paint(g, c(0x93D6CF, 175), c(0xC4ECE8, 90));
        double px = stageX + sw * 0.86;
        pagoda(g, px, far.y(px) + h * 0.012, m * 0.27, c(0x4FAFA8, 150));
        mist(g, w, h * 0.54, h * 0.20, 150);

        // Middle hills with a torii.
        Ridge mid = new Ridge(w, h, h * 0.78, h * 0.15, 5.4, 0.0049, false);
        mid.paint(g, c(0x73C8C0, 205), c(0xA7DED8, 150));
        double tx = stageX + sw * 0.10;
        torii(g, tx, mid.y(tx) + h * 0.012, m * 0.20, c(0x2F9C94, 190));
        mist(g, w, h * 0.70, h * 0.14, 110);

        // The ground the ninjas stand on.
        double gy = lobbyGround(h);
        Ridge ground = new Ridge(w, h, gy + h * 0.02, h * 0.035, 0.7, 0.0035, false);
        ground.paint(g, c(0x6CC6BE, 245), c(0x52B2AA, 255));

        // Falling blossoms and a blossom branch from the top right corner.
        petals(g, 16, 7, stageX + sw * 0.05, h * 0.10, sw * 0.9, h * 0.72, m * 0.011, 200);
        branch(g, w + 12, h * 0.015, w - sw * 0.16, h * 0.02, w - sw * 0.40, h * 0.17, Math.max(5, m * 0.012), 11, 235);

        // Two or three Sudoku tiles floating in the sky.
        int tile = Math.max(30, (int) (m * 0.07));
        int[][] spots = {
            {(int) (stageX + sw * 0.07), (int) (h * 0.38), 5, -7},
            {(int) (stageX + sw * 0.90 - tile), (int) (h * 0.14), 8, 8},
            {(int) (stageX + sw * 0.62), (int) (h * 0.08), 3, 5},
        };
        for (int[] s : spots) {
            if (s[0] > stageX + 20 && s[0] + tile < w - 10 && s[1] > 10) {
                tileAt(g, s[0], s[1], tile, s[2], s[3]);
            }
        }

        // The controls on the left stay calm: the scene fades to the page colour there.
        float fadeEnd = (float) (stageX + sw * 0.08);
        g.setPaint(new GradientPaint(0, 0, new Color(240, 250, 249, 250), fadeEnd, 0, new Color(240, 250, 249, 0)));
        g.fillRect(0, 0, (int) fadeEnd + 1, h);
        g.setPaint(new GradientPaint(0, 0, new Color(238, 249, 248), 0, h, new Color(246, 252, 252)));
        g.fillRect(0, 0, Math.max(0, stageX - 60), h);
        g.setPaint(new GradientPaint(Math.max(0, stageX - 60), 0, new Color(238, 249, 248, 255), fadeEnd, 0,
                new Color(238, 249, 248, 0)));
        g.fillRect(Math.max(0, stageX - 60), 0, (int) fadeEnd - Math.max(0, stageX - 60) + 1, h);
        g.dispose();
        return image;
    }

    // ----------------------------------------------------------------------------------------------- multiplayer

    public static int multiplayerGround(int h) {
        return (int) (h * 0.86);
    }

    // The entry page: the stage (left, x < stageX) is a bright morning; the cards (right) sit on a calmer surface.
    public static BufferedImage multiplayer(int w, int h, int stageX) {
        BufferedImage image = canvas(w, h);
        Graphics2D g = open(image);
        int m = Math.min(w, h);
        stageX = Math.max(260, stageX);
        double scx = stageX * 0.52;
        sky(g, w, h, 0xC3ECE7, 0xE2F6F3, 0xF4FCFB);
        // a pale sun, slightly warm, behind the ninja
        double sr = Math.min(stageX, h) * 0.17;
        g.setPaint(new RadialGradientPaint((float) scx, (float) (h * 0.30), (float) (sr * 3.6), new float[] {0f, 0.3f, 1f},
                new Color[] {new Color(255, 250, 232, 235), new Color(255, 255, 255, 90), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(scx - sr * 3.6, h * 0.30 - sr * 3.6, sr * 7.2, sr * 7.2));
        g.setColor(new Color(255, 252, 238, 245));
        g.fill(new Ellipse2D.Double(scx - sr, h * 0.30 - sr, sr * 2, sr * 2));
        cloud(g, stageX * 0.2, h * 0.20, stageX * 0.34, 170);
        cloud(g, stageX * 0.86, h * 0.34, stageX * 0.26, 130);

        Ridge far = new Ridge(w, h, h * 0.66, h * 0.24, 0.6, 0.0053, true);
        far.paint(g, c(0x93D6CF, 170), c(0xC4ECE8, 90));
        double px = w * 0.62;
        pagoda(g, px, far.y(px) + h * 0.012, m * 0.22, c(0x4FAFA8, 120));
        mist(g, w, h * 0.54, h * 0.20, 150);
        Ridge mid = new Ridge(w, h, h * 0.79, h * 0.14, 3.3, 0.0046, false);
        mid.paint(g, c(0x73C8C0, 200), c(0xA7DED8, 150));
        // a big torii framing the ninja
        torii(g, scx + stageX * 0.0, multiplayerGround(h) - h * 0.015, m * 0.62, c(0x2F9C94, 90));
        mist(g, w, h * 0.72, h * 0.12, 100);
        Ridge ground = new Ridge(w, h, multiplayerGround(h) + h * 0.02, h * 0.03, 2.4, 0.0036, false);
        ground.paint(g, c(0x6CC6BE, 245), c(0x52B2AA, 255));

        petals(g, 14, 21, 10, h * 0.1, stageX, h * 0.7, m * 0.011, 200);
        branch(g, -12, h * 0.02, stageX * 0.20, h * 0.01, stageX * 0.46, h * 0.17, Math.max(5, m * 0.013), 5, 235);

        // Calm surface under the cards on the right.
        float f0 = (float) (stageX - 90);
        g.setPaint(new GradientPaint(f0, 0, new Color(238, 249, 248, 0), (float) (stageX + 120), 0,
                new Color(238, 249, 248, 245)));
        g.fillRect((int) f0, 0, w - (int) f0, h);
        g.setPaint(new GradientPaint(0, 0, new Color(236, 248, 247, 0), 0, h, new Color(246, 252, 252, 0)));
        g.dispose();
        return image;
    }

    // ------------------------------------------------------------------------------------------------------ game

    // The calmest scene. Detail lives only in the lower part (hills, a faint pagoda); the board gets a soft spotlight.
    public static BufferedImage game(int w, int h, int boardCx, int boardCy, int boardSize) {
        BufferedImage image = canvas(w, h);
        Graphics2D g = open(image);
        int m = Math.min(w, h);
        scenic(g, w, h, 0.5, m * 0.22, false);

        // the soft spotlight behind the board keeps the middle calm
        float radius = Math.max(boardSize * 1.0f, m * 0.62f);
        g.setPaint(new RadialGradientPaint(boardCx, boardCy, radius, new float[] {0f, 0.62f, 1f},
                new Color[] {new Color(247, 253, 252, 235), new Color(247, 253, 252, 150), new Color(247, 253, 252, 0)}));
        g.fill(new Ellipse2D.Float(boardCx - radius, boardCy - radius, radius * 2, radius * 2));

        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(20, 168, 156, 26));
        float pad = boardSize * 0.06f;
        g.draw(new RoundRectangle2D.Float(boardCx - boardSize / 2f - pad, boardCy - boardSize / 2f - pad,
                boardSize + 2 * pad, boardSize + 2 * pad, 26, 26));
        g.dispose();
        return image;
    }

    private static void tileAt(Graphics2D g, int x, int y, int size, int digit, int degrees) {
        java.awt.geom.AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(degrees), x + size / 2.0, y + size / 2.0);
        g.setColor(new Color(14, 95, 92, 16));
        g.fill(new RoundRectangle2D.Float(x, y + size * 0.07f, size, size, size * 0.24f, size * 0.24f));
        g.setColor(new Color(255, 255, 255, 215));
        g.fill(new RoundRectangle2D.Float(x, y, size, size, size * 0.24f, size * 0.24f));
        g.setColor(new Color(0xC3E3DF));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, size - 1, size - 1, size * 0.24f, size * 0.24f));
        g.setFont(Theme.bold((int) (size * 0.56)));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(20, 168, 156, 150));
        String text = String.valueOf(digit);
        g.drawString(text, x + (size - fm.stringWidth(text)) / 2, y + (size + fm.getAscent() - fm.getDescent()) / 2);
        g.setTransform(old);
    }


    // ------------------------------------------------------------------------------------------------ v6 moods

    // The restrained background system: every screen picks the mood that fits its job instead of repeating one landscape.
    public enum Mood { WELCOME, CALM, CELEBRATE, MUTED }

    public static BufferedImage mood(int w, int h, Mood mood) {
        switch (mood) {
            case WELCOME:
                return soft(w, h);
            case CELEBRATE:
                return celebrate(w, h);
            case MUTED:
                return muted(w, h);
            default:
                return quiet(w, h);
        }
    }

    // Faint Sudoku grid over a very light surface, a few ghost digits near the edges, one low hill line.
    private static void gridLayer(Graphics2D g, int w, int h, int alphaLine, long seed, int digitAlpha, int rgb) {
        int cell = Math.max(46, Math.min(w, h) / 12);
        int ox = (w % (cell * 3)) / 2;
        int oy = (h % (cell * 3)) / 2;
        for (int i = -3; i * cell + ox <= w + cell; i++) {
            int x = ox + i * cell;
            boolean box = Math.floorMod(i, 3) == 0;
            g.setStroke(new BasicStroke(box ? 1.6f : 1f));
            g.setColor(c(rgb, box ? alphaLine * 2 : alphaLine));
            g.drawLine(x, 0, x, h);
        }
        for (int j = -3; j * cell + oy <= h + cell; j++) {
            int y = oy + j * cell;
            boolean box = Math.floorMod(j, 3) == 0;
            g.setStroke(new BasicStroke(box ? 1.6f : 1f));
            g.setColor(c(rgb, box ? alphaLine * 2 : alphaLine));
            g.drawLine(0, y, w, y);
        }
        java.util.Random r = new java.util.Random(seed);
        g.setFont(Theme.bold((int) (cell * 0.5)));
        FontMetrics fm = g.getFontMetrics();
        int cols = w / cell + 1;
        int rows = h / cell + 1;
        for (int k = 0; k < cols * rows / 5; k++) {
            int cx = r.nextInt(cols);
            int cy = r.nextInt(rows);
            int digit = 1 + r.nextInt(9);
            int x = ox + (cx - 1) * cell;
            int y = oy + (cy - 1) * cell;
            g.setColor(c(rgb, digitAlpha));
            String t = String.valueOf(digit);
            g.drawString(t, x + (cell - fm.stringWidth(t)) / 2, y + (cell + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    private static void veil(Graphics2D g, int w, int h, double cx, double cy, double rx, double ry, int rgb, int alpha) {
        java.awt.geom.AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.scale(1.0, ry / rx);
        g.setPaint(new RadialGradientPaint(0, 0, (float) rx, new float[] {0f, 0.55f, 1f},
                new Color[] {c(rgb, alpha), c(rgb, (int) (alpha * 0.7)), c(rgb, 0)}));
        g.fill(new Ellipse2D.Double(-rx, -rx, rx * 2, rx * 2));
        g.setTransform(old);
    }

    public static BufferedImage quiet(int w, int h) {
        BufferedImage image = canvas(w, h);
        Graphics2D g = open(image);
        sky(g, w, h, 0xF8FDFC, 0xF0FAF8, 0xE4F5F2);
        gridLayer(g, w, h, 9, 11, 16, 0x14A89C);
        // keep the middle of the page calm and readable
        veil(g, w, h, w / 2.0, h * 0.5, w * 0.62, h * 0.62, 0xF8FDFC, 215);
        moon(g, w * 0.92, h * 0.12, Math.min(w, h) * 0.03, 70);
        Ridge far = new Ridge(w, h, h * 0.93, h * 0.10, 1.4, 0.005, true);
        far.paint(g, c(0x9BD9D2, 70), c(0xCBEDEA, 30));
        Ridge near = new Ridge(w, h, h * 1.0, h * 0.05, 3.1, 0.0046, false);
        near.paint(g, c(0x86D0C9, 60), c(0xB3E2DD, 30));
        g.dispose();
        return image;
    }

    public static BufferedImage celebrate(int w, int h) {
        BufferedImage image = canvas(w, h);
        Graphics2D g = open(image);
        sky(g, w, h, 0xFFFBEA, 0xF4FBF3, 0xE2F6F2);
        // soft rays fanning out behind the winner
        double cx = w * 0.5;
        double cy = h * 0.42;
        double len = Math.hypot(w, h);
        int rays = 18;
        for (int i = 0; i < rays; i++) {
            double a0 = Math.PI * 2 * i / rays;
            double a1 = a0 + Math.PI / rays;
            java.awt.geom.Path2D ray = new java.awt.geom.Path2D.Double();
            ray.moveTo(cx, cy);
            ray.lineTo(cx + Math.cos(a0) * len, cy + Math.sin(a0) * len);
            ray.lineTo(cx + Math.cos(a1) * len, cy + Math.sin(a1) * len);
            ray.closePath();
            g.setPaint(new RadialGradientPaint((float) cx, (float) cy, (float) (len * 0.7), new float[] {0f, 1f},
                    new Color[] {c(0xFFD34E, 46), c(0xFFD34E, 0)}));
            g.fill(ray);
        }
        veil(g, w, h, cx, cy, Math.min(w, h) * 0.55, Math.min(w, h) * 0.5, 0xFFFFFF, 230);
        // confetti: small tiles, discs and ribbons, kept to the outer parts of the page
        java.util.Random r = new java.util.Random(7);
        int[] palette = {0x14A89C, 0xFFB62E, 0xFF6F61, 0x8B6FE8, 0x3FA7F5, 0x34C28B};
        int n = Math.max(30, w * h / 14000);
        for (int i = 0; i < n; i++) {
            double x = r.nextDouble() * w;
            double y = r.nextDouble() * h;
            double dx = (x - cx) / (w * 0.5);
            double dy = (y - cy) / (h * 0.5);
            if (dx * dx + dy * dy < 0.42) {
                continue;
            }
            int col = palette[r.nextInt(palette.length)];
            double size = 5 + r.nextDouble() * 9;
            java.awt.geom.AffineTransform old = g.getTransform();
            g.rotate(r.nextDouble() * Math.PI, x, y);
            g.setColor(c(col, 120 + r.nextInt(70)));
            switch (r.nextInt(3)) {
                case 0:
                    g.fill(new RoundRectangle2D.Double(x, y, size, size * 0.55, 3, 3));
                    break;
                case 1:
                    g.fill(new Ellipse2D.Double(x, y, size * 0.7, size * 0.7));
                    break;
                default:
                    g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(new java.awt.geom.QuadCurve2D.Double(x, y, x + size, y - size * 0.8, x + size * 2, y));
                    break;
            }
            g.setTransform(old);
        }
        Ridge far = new Ridge(w, h, h * 0.95, h * 0.08, 0.9, 0.005, true);
        far.paint(g, c(0x9BD9D2, 80), c(0xCBEDEA, 30));
        g.dispose();
        return image;
    }

    public static BufferedImage muted(int w, int h) {
        BufferedImage image = canvas(w, h);
        Graphics2D g = open(image);
        sky(g, w, h, 0xE9F0F3, 0xE1EBEE, 0xD3E1E5);
        gridLayer(g, w, h, 8, 23, 12, 0x5E7F8A);
        veil(g, w, h, w / 2.0, h * 0.5, w * 0.6, h * 0.6, 0xEAF1F3, 200);
        // a few slanting lines of light rain
        java.util.Random r = new java.util.Random(5);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < Math.max(24, w * h / 26000); i++) {
            double x = r.nextDouble() * w;
            double y = r.nextDouble() * h;
            g.setColor(c(0x7F9CA8, 36));
            g.draw(new java.awt.geom.Line2D.Double(x, y, x - 7, y + 20));
        }
        Ridge far = new Ridge(w, h, h * 0.94, h * 0.09, 2.4, 0.005, true);
        far.paint(g, c(0x9DB4BD, 90), c(0xBFD0D6, 40));
        g.dispose();
        return image;
    }

}
