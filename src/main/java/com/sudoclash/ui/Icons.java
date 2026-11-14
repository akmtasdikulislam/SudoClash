package com.sudoclash.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

public final class Icons {
    public enum Type {
        PLAY, BACK, UNDO, CLEAR, CHECK, HINT, PENCIL, USER, USERS, CLOCK, TROPHY, SWORDS, GRID, PLUG,
        ROOM, COPY, LOGOUT, POWER, WARNING, INFO, CROSS, SERVER, FLAG, CROWN, PLUS, LINK, LOGO,
        LEVEL_1, LEVEL_2, LEVEL_3, EYE, EYE_OFF, SOUND, MUTE, SMILE
    }

    private Icons() {
    }

    public static Icon icon(Type type, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                paint((Graphics2D) g, type, x, y, size, color);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    public static void paint(Graphics2D g0, Type type, double x, double y, double size, Color color) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        g.translate(x, y);
        g.scale(size / 24.0, size / 24.0);
        g.setColor(color);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {
            case PLAY:
                Path2D play = poly(true, 7, 4.5, 19.5, 12, 7, 19.5);
                g.fill(play);
                g.draw(play);
                break;
            case BACK:
                g.draw(new Line2D.Double(20, 12, 5, 12));
                g.draw(poly(false, 11, 6, 5, 12, 11, 18));
                break;
            case UNDO:
                g.draw(poly(false, 9, 4, 4, 9, 9, 14));
                Path2D undo = new Path2D.Double();
                undo.moveTo(4, 9);
                undo.lineTo(14, 9);
                undo.curveTo(19.5, 9, 20, 19, 13.5, 19);
                undo.lineTo(9, 19);
                g.draw(undo);
                break;
            case CLEAR:
                g.draw(new Line2D.Double(4, 7, 20, 7));
                g.draw(poly(false, 9, 7, 9, 4, 15, 4, 15, 7));
                g.draw(poly(true, 6, 7, 7, 20, 17, 20, 18, 7));
                g.draw(new Line2D.Double(10.5, 11, 10.5, 16));
                g.draw(new Line2D.Double(13.5, 11, 13.5, 16));
                break;
            case CHECK:
                g.draw(poly(false, 4.5, 12.5, 9.5, 17.5, 19.5, 6.5));
                break;
            case SMILE:
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.fill(new Ellipse2D.Double(8, 9, 2.4, 2.8));
                g.fill(new Ellipse2D.Double(13.6, 9, 2.4, 2.8));
                g.draw(new Arc2D.Double(7.5, 8.5, 9, 8.5, 200, 140, Arc2D.OPEN));
                break;
            case SOUND:
            case MUTE:
                g.draw(poly(true, 4, 9.5, 8, 9.5, 13, 5, 13, 19, 8, 14.5, 4, 14.5));
                if (type == Type.SOUND) {
                    g.draw(new Arc2D.Double(10, 8, 7, 8, -55, 110, Arc2D.OPEN));
                    g.draw(new Arc2D.Double(8, 4.5, 13, 15, -55, 110, Arc2D.OPEN));
                } else {
                    g.draw(new Line2D.Double(16, 9.5, 21.5, 14.5));
                    g.draw(new Line2D.Double(21.5, 9.5, 16, 14.5));
                }
                break;
            case EYE:
            case EYE_OFF:
                Path2D eye = new Path2D.Double();
                eye.moveTo(2, 12);
                eye.curveTo(5, 6.5, 8.5, 5, 12, 5);
                eye.curveTo(15.5, 5, 19, 6.5, 22, 12);
                eye.curveTo(19, 17.5, 15.5, 19, 12, 19);
                eye.curveTo(8.5, 19, 5, 17.5, 2, 12);
                eye.closePath();
                g.draw(eye);
                g.draw(new Ellipse2D.Double(8.8, 8.8, 6.4, 6.4));
                if (type == Type.EYE_OFF) {
                    g.draw(new Line2D.Double(4, 4, 20, 20));
                }
                break;
            case HINT:
                Path2D bulb = new Path2D.Double();
                bulb.moveTo(9, 16.5);
                bulb.curveTo(9, 14.5, 5.5, 13.5, 5.5, 9.5);
                bulb.curveTo(5.5, 5.5, 8.5, 3, 12, 3);
                bulb.curveTo(15.5, 3, 18.5, 5.5, 18.5, 9.5);
                bulb.curveTo(18.5, 13.5, 15, 14.5, 15, 16.5);
                bulb.closePath();
                g.draw(bulb);
                g.draw(new Line2D.Double(9.5, 19.5, 14.5, 19.5));
                g.draw(new Line2D.Double(10.5, 22.3, 13.5, 22.3));
                break;
            case PENCIL:
                g.draw(poly(true, 4, 20, 5, 15, 16, 4, 20, 8, 9, 19));
                g.draw(new Line2D.Double(13, 7, 17, 11));
                break;
            case USER:
                person(g, 0, 0, 1.0);
                break;
            case USERS:
                person(g, -2.5, 1.5, 0.78);
                person(g, 8, 1.5, 0.78);
                break;
            case CLOCK:
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(poly(false, 12, 7, 12, 12, 15.5, 14));
                break;
            case TROPHY:
                Path2D cup = new Path2D.Double();
                cup.moveTo(7, 4);
                cup.lineTo(17, 4);
                cup.lineTo(17, 9);
                cup.curveTo(17, 13.5, 7, 13.5, 7, 9);
                cup.closePath();
                g.draw(cup);
                Path2D left = new Path2D.Double();
                left.moveTo(7, 6);
                left.lineTo(4, 6);
                left.curveTo(4, 10, 5.5, 11.5, 8, 12);
                g.draw(left);
                Path2D right = new Path2D.Double();
                right.moveTo(17, 6);
                right.lineTo(20, 6);
                right.curveTo(20, 10, 18.5, 11.5, 16, 12);
                g.draw(right);
                g.draw(new Line2D.Double(12, 13.5, 12, 18));
                g.draw(new Line2D.Double(8, 20.5, 16, 20.5));
                break;
            case SWORDS:
                g.draw(new Line2D.Double(5, 5, 19, 19));
                g.draw(new Line2D.Double(19, 5, 5, 19));
                g.draw(new Line2D.Double(19.5, 14.5, 14.5, 19.5));
                g.draw(new Line2D.Double(4.5, 14.5, 9.5, 19.5));
                break;
            case GRID:
                g.draw(new RoundRectangle2D.Double(3, 3, 18, 18, 4, 4));
                g.draw(new Line2D.Double(9, 3, 9, 21));
                g.draw(new Line2D.Double(15, 3, 15, 21));
                g.draw(new Line2D.Double(3, 9, 21, 9));
                g.draw(new Line2D.Double(3, 15, 21, 15));
                break;
            case PLUG:
                g.draw(new Line2D.Double(9, 3, 9, 8));
                g.draw(new Line2D.Double(15, 3, 15, 8));
                g.draw(new RoundRectangle2D.Double(6, 8, 12, 6.5, 3, 3));
                Path2D cable = new Path2D.Double();
                cable.moveTo(12, 14.5);
                cable.lineTo(12, 17);
                cable.curveTo(12, 20, 16, 19, 16, 22);
                g.draw(cable);
                break;
            case ROOM:
                g.draw(new RoundRectangle2D.Double(6, 3, 12, 18, 2, 2));
                g.fill(new Ellipse2D.Double(13.6, 11, 2.2, 2.2));
                g.draw(new Line2D.Double(3, 21.5, 21, 21.5));
                break;
            case COPY:
                g.draw(new RoundRectangle2D.Double(9, 3, 12, 13, 3, 3));
                g.draw(poly(false, 15, 20, 6, 20, 4, 18, 4, 8));
                break;
            case LOGOUT:
                g.draw(poly(false, 10, 4, 5, 4, 5, 20, 10, 20));
                g.draw(new Line2D.Double(10, 12, 20, 12));
                g.draw(poly(false, 16, 8, 20, 12, 16, 16));
                break;
            case POWER:
                g.draw(new Arc2D.Double(5, 5, 14, 14, 130, 280, Arc2D.OPEN));
                g.draw(new Line2D.Double(12, 3, 12, 12));
                break;
            case WARNING:
                g.draw(poly(true, 12, 3.5, 21.5, 20, 2.5, 20));
                g.draw(new Line2D.Double(12, 9.5, 12, 14));
                g.fill(new Ellipse2D.Double(11, 16, 2, 2));
                break;
            case INFO:
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Line2D.Double(12, 11, 12, 17));
                g.fill(new Ellipse2D.Double(11, 6.6, 2, 2));
                break;
            case CROSS:
                g.draw(new Line2D.Double(6, 6, 18, 18));
                g.draw(new Line2D.Double(18, 6, 6, 18));
                break;
            case SERVER:
                g.draw(new RoundRectangle2D.Double(4, 4, 16, 6.5, 3, 3));
                g.draw(new RoundRectangle2D.Double(4, 13.5, 16, 6.5, 3, 3));
                g.fill(new Ellipse2D.Double(7, 6.1, 2.2, 2.2));
                g.fill(new Ellipse2D.Double(7, 15.6, 2.2, 2.2));
                break;
            case FLAG:
                g.draw(new Line2D.Double(6, 3, 6, 21));
                g.draw(poly(true, 6, 4.5, 19, 4.5, 15, 9, 19, 13.5, 6, 13.5));
                break;
            case CROWN:
                g.draw(poly(true, 3.5, 18.5, 3.5, 8, 8.5, 12.5, 12, 5.5, 15.5, 12.5, 20.5, 8, 20.5, 18.5));
                break;
            case PLUS:
                g.draw(new Line2D.Double(12, 5, 12, 19));
                g.draw(new Line2D.Double(5, 12, 19, 12));
                break;
            case LINK:
                g.draw(new RoundRectangle2D.Double(2.5, 8.5, 11.5, 7, 7, 7));
                g.draw(new RoundRectangle2D.Double(10, 8.5, 11.5, 7, 7, 7));
                break;
            case LOGO:
                g.draw(new RoundRectangle2D.Double(3, 3, 18, 18, 5, 5));
                g.draw(new Line2D.Double(9, 3, 9, 21));
                g.draw(new Line2D.Double(15, 3, 15, 21));
                g.draw(new Line2D.Double(3, 9, 21, 9));
                g.draw(new Line2D.Double(3, 15, 21, 15));
                g.fill(new RoundRectangle2D.Double(9.4, 9.4, 5.2, 5.2, 2, 2));
                break;
            case LEVEL_1:
            case LEVEL_2:
            case LEVEL_3:
                int level = type.ordinal() - Type.LEVEL_1.ordinal() + 1;
                for (int i = 0; i < 3; i++) {
                    double height = 7 + 5 * i;
                    g.setColor(i < level ? color : Theme.alpha(color, 60));
                    g.fill(new RoundRectangle2D.Double(3.5 + i * 6.5, 21 - height, 4.5, height, 2, 2));
                }
                break;
            default:
                break;
        }
        g.dispose();
    }

    private static Path2D poly(boolean closed, double... p) {
        Path2D path = new Path2D.Double();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) {
            path.lineTo(p[i], p[i + 1]);
        }
        if (closed) {
            path.closePath();
        }
        return path;
    }

    private static void person(Graphics2D g0, double dx, double dy, double scale) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(dx, dy);
        g.translate(12 * (1 - scale), 12 * (1 - scale));
        g.scale(scale, scale);
        g.draw(new Ellipse2D.Double(8, 3.5, 8, 8));
        Path2D body = new Path2D.Double();
        body.moveTo(4.5, 21);
        body.curveTo(4.5, 15.5, 19.5, 15.5, 19.5, 21);
        g.draw(body);
        g.dispose();
    }
}
