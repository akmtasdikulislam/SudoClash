package com.sudoclash.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

public final class Theme {
    public static final Color BG = new Color(0xE6F6F4);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xD3E9E7);

    public static final Color FOREST = new Color(0x0A4B46);
    public static final Color FOREST_2 = new Color(0x137A72);
    public static final Color EMERALD = new Color(0x14A89C);
    public static final Color EMERALD_HOVER = new Color(0x2BBDB1);
    public static final Color EMERALD_DARK = new Color(0x0E857B);
    public static final Color TEAL = new Color(0x0E7C8F);
    public static final Color MINT = new Color(0xDCF3F1);
    public static final Color MINT_LIGHT = new Color(0xEEF9F8);
    public static final Color MINT_MID = new Color(0xB6E5E0);

    public static final Color TEXT = new Color(0x1B2B2B);
    public static final Color MUTED = new Color(0x5E7573);
    public static final Color DISABLED = new Color(0xA3B9B7);
    public static final Color DISABLED_BG = new Color(0xE6F0EF);

    public static final Color ERROR = new Color(0xDC2626);
    public static final Color ERROR_BG = new Color(0xFEE2E2);
    public static final Color AMBER = new Color(0xD97706);
    public static final Color WARN_TEXT = new Color(0x92400E);
    public static final Color WARN_BG = new Color(0xFEF3C7);
    public static final Color OK = new Color(0x16A34A);
    public static final Color OK_BG = new Color(0xDCFCE7);

    // Pink marks a wrong number (as in the reference), the INK colour is the near-black of the given digits.
    public static final Color PINK = new Color(0xFFD0DE);
    public static final Color GRID_THIN = new Color(0xD3E9E7);
    public static final Color GRID_BOX = new Color(0x8FA7A5);

    public static final String FAMILY = pickFamily();

    private Theme() {
    }

    private static String pickFamily() {
        Set<String> installed = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String name : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Inter", "Ubuntu", "Noto Sans"}) {
            if (installed.contains(name)) {
                return name;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font plain(int size) {
        return new Font(FAMILY, Font.PLAIN, size);
    }

    public static Font bold(int size) {
        return new Font(FAMILY, Font.BOLD, size);
    }

    // The "SudoClash" wordmark: bold, dark teal, no letter spacing (like the ninja artwork's logo).
    public static Font wordmark(int size) {
        return bold(size);
    }

    public static Font title() {
        return spaced(plain(36), 0.03f);
    }

    public static Font heading() {
        return bold(24);
    }

    public static Font section() {
        return spaced(bold(12), 0.08f);
    }

    // Letter-spaced text (captions, the SUDOCLASH wordmark). Swing measures it correctly.
    public static Font spaced(Font font, float tracking) {
        java.util.Map<java.awt.font.TextAttribute, Object> attributes = new java.util.HashMap<>();
        attributes.put(java.awt.font.TextAttribute.TRACKING, tracking);
        return font.deriveFont(attributes);
    }

    // The light, airy heading style of the reference design.
    public static Font light(int size) {
        return new Font(FAMILY, Font.PLAIN, size);
    }

    public static Font body() {
        return plain(15);
    }

    public static Font small() {
        return plain(13);
    }

    public static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // Vivid colours for icons and badges (kept clean and friendly, like the reference sheet).
    public static final Color GREEN = new Color(0x1FA36B);
    public static final Color BLUE = new Color(0x2F80ED);
    public static final Color PURPLE = new Color(0x8B5CF6);
    public static final Color GOLD = new Color(0xF5B301);
    public static final Color ORANGE = new Color(0xF2994A);
    public static final Color CORAL = new Color(0xEF5A5A);

    public static Color iconColor(Icons.Type type) {
        if (type == null) {
            return EMERALD_DARK;
        }
        switch (type) {
            case PLAY: case CHECK: case PLUS: case SMILE: case FLAG:
                return GREEN;
            case UNDO: case USERS: case COPY: case LINK: case INFO: case SOUND: case LEVEL_2:
                return BLUE;
            case CLEAR: case SWORDS: case POWER: case CROSS: case WARNING: case LEVEL_3:
                return CORAL;
            case HINT: case TROPHY: case CROWN:
                return GOLD;
            case PENCIL: case CLOCK: case LOGOUT:
                return ORANGE;
            case USER: case ROOM: case SERVER:
                return PURPLE;
            case LEVEL_1:
                return GREEN;
            default:
                return EMERALD;
        }
    }

    public static Color darker(Color c, float amount) {
        return blend(c, Color.BLACK, amount);
    }

    public static Color lighter(Color c, float amount) {
        return blend(c, Color.WHITE, amount);
    }

    public static Color blend(Color a, Color b, float t) {
        float k = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * k),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * k));
    }

    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    // A clean white card: thin aqua border and a very soft shadow underneath. The radius is capped so every
    // container shares one gentle corner shape.
    public static void paintCard(Graphics2D g, int x, int y, int w, int h, int arc, int shadow, Color fill, Color border) {
        arc = Math.min(arc, 18);
        int layers = Math.max(1, Math.min(shadow, 6));
        for (int i = layers; i >= 1; i--) {
            int a = Math.round(10f * (layers - i + 1) / layers);
            g.setColor(new Color(14, 95, 92, a));
            g.fill(new RoundRectangle2D.Float(x - i * 0.5f, y - i * 0.5f + layers * 0.7f, w + i, h + i, arc + i, arc + i));
        }
        RoundRectangle2D body = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
        g.setColor(fill);
        g.fill(body);
        if (border != null) {
            g.setColor(border);
            g.setStroke(new java.awt.BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, arc, arc));
        }
    }

    // A pale number tile (the keys and chips of the reference). pressed = pushed down, lift = hover glow 0..1.
    public static void paintTile(Graphics2D g, double x, double y, double w, double h, int arc, Color face,
                                 float lift, boolean pressed) {
        double dy = pressed ? 1.5 : 0;
        if (!pressed) {
            g.setColor(new Color(14, 95, 92, Math.round(22 + 22 * lift)));
            g.fill(new RoundRectangle2D.Double(x, y + 2.5 - lift, w, h, arc, arc));
        }
        g.setColor(face);
        g.fill(new RoundRectangle2D.Double(x, y + dy - lift * 1.0, w, h, arc, arc));
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new java.awt.BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(x + 0.5, y + dy - lift + 0.5, w - 1, h - 1, arc, arc));
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static void installToolTipStyle() {
        UIManager.put("ToolTip.font", plain(13));
        UIManager.put("ToolTip.background", FOREST);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(5, 9, 5, 9));
        ToolTipManager.sharedInstance().setInitialDelay(350);
    }

    public static Color medalColor(int place) {
        switch (place) {
            case 1:
                return new Color(0xCA8A04);
            case 2:
                return new Color(0x6B7280);
            case 3:
                return new Color(0xB45309);
            default:
                return EMERALD_DARK;
        }
    }
}
