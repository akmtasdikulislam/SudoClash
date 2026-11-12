package com.sudoclash.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

// The ninja artwork of the SudoClash identity. Images are loaded once and scaled with a smooth, step-wise
// filter (never stretched: the aspect ratio is always kept). Purely decorative: nothing here touches the game.
public final class Art {
    public enum Image {
        HERO("mascot_hero"), HERO_FLIP("mascot_hero_flip"), TIMER("mascot_timer"), LOGIN("mascot_login"), MULTI("mascot_multi"), DONE("mascot_done"),
        AVATAR("mascot_avatar"), HOME("mascot_home"), TROPHY("trophy"),
        HAPPY("ninja_happy"), THINK("ninja_think"), THINK2("ninja_think2"), CRY("ninja_cry"), CRY2("ninja_cry2"),
        CHEER("ninja_cheer"), CUP("ninja_trophy"), MEDITATE("ninja_meditate"), LAPTOP("ninja_laptop"), COOL("ninja_cool"),
        ANGRY("ninja_angry"), SLEEP("ninja_sleep"), STANCE("ninja_stance"), BOARD("ninja_board"), DASH("ninja_dash"),
        SWORD("ninja_sword"), KICK("ninja_kick"), PEACE("ninja_peace"), ARMSUP("ninja_armsup"), SWEAT("ninja_sweat"),
        SHURIKEN("ninja_shuriken");

        private final String file;

        Image(String file) {
            this.file = file;
        }
    }

    private static final Map<Image, BufferedImage> ORIGINALS = new HashMap<>();
    private static final Map<String, BufferedImage> SCALED = new HashMap<>();

    private Art() {
    }

    public static synchronized BufferedImage original(Image image) {
        BufferedImage loaded = ORIGINALS.get(image);
        if (loaded == null && !ORIGINALS.containsKey(image)) {
            try (java.io.InputStream in = Art.class.getResourceAsStream("/img/" + image.file + ".png")) {
                loaded = in == null ? null : ImageIO.read(in);
            } catch (Exception e) {
                loaded = null;
            }
            ORIGINALS.put(image, loaded);
        }
        return loaded;
    }

    public static double ratio(Image image) {
        BufferedImage o = original(image);
        return o == null ? 1.0 : o.getWidth() / (double) o.getHeight();
    }

    // The image scaled to w x h (halving steps first, so downscaling stays sharp).
    private static synchronized BufferedImage scaled(Image image, int w, int h) {
        String key = image.name() + ":" + w + "x" + h;
        BufferedImage cached = SCALED.get(key);
        if (cached != null) {
            return cached;
        }
        BufferedImage src = original(image);
        if (src == null) {
            return null;
        }
        BufferedImage cur = src;
        while (cur.getWidth() / 2 >= w && cur.getHeight() / 2 >= h) {
            cur = step(cur, cur.getWidth() / 2, cur.getHeight() / 2, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        cur = step(cur, w, h, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        if (SCALED.size() > 40) {
            SCALED.clear();
        }
        SCALED.put(key, cur);
        return cur;
    }

    private static BufferedImage step(BufferedImage src, int w, int h, Object interpolation) {
        BufferedImage out = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
        g.dispose();
        return out;
    }

    // Draws the image as large as possible inside the box, centred, keeping its proportions.
    public static void paintFit(Graphics2D g, Image image, int x, int y, int w, int h) {
        BufferedImage o = original(image);
        if (o == null || w < 4 || h < 4) {
            return;
        }
        double k = Math.min(w / (double) o.getWidth(), h / (double) o.getHeight());
        int dw = Math.max(1, (int) Math.round(o.getWidth() * k));
        int dh = Math.max(1, (int) Math.round(o.getHeight() * k));
        BufferedImage s = scaled(image, dw, dh);
        if (s != null) {
            g.drawImage(s, x + (w - dw) / 2, y + (h - dh) / 2, null);
        }
    }

    // The round ninja face used as an avatar (profile, user chip).
    public static void paintAvatar(Graphics2D g, int x, int y, int d, boolean ring) {
        java.awt.Shape circle = new Ellipse2D.Float(x, y, d, d);
        g.setColor(Theme.MINT);
        g.fill(circle);
        java.awt.Shape oldClip = g.getClip();
        g.clip(circle);
        paintFit(g, Image.AVATAR, x, y, d, d);
        g.setClip(oldClip);
        if (ring) {
            g.setColor(Theme.BORDER);
            g.setStroke(new java.awt.BasicStroke(Math.max(1f, d / 28f)));
            g.draw(new Ellipse2D.Float(x + 0.5f, y + 0.5f, d - 1, d - 1));
        }
    }

    public static javax.swing.Icon avatarIcon(int d) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                Theme.quality(g);
                paintAvatar(g, x, y, d, true);
                g.dispose();
            }

            @Override
            public int getIconWidth() {
                return d;
            }

            @Override
            public int getIconHeight() {
                return d;
            }
        };
    }

    // A picture that grows with the space it gets, up to a maximum size, and never distorts.
    public static class Pic extends JComponent {
        private static final long serialVersionUID = 1L;

        private final Image image;
        private final int maxW;
        private final int maxH;

        public Pic(Image image, int maxW, int maxH) {
            this.image = image;
            this.maxW = maxW;
            this.maxH = maxH;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            double r = ratio(image);
            int h = (int) Math.round(Math.min(maxH, maxW / r));
            return new Dimension((int) Math.round(h * r), h);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(40, 40);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(maxW, maxH);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.quality(g);
            paintFit(g, image, 0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }
}
