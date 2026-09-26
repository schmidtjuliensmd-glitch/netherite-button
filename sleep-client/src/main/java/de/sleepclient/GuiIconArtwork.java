package de.sleepclient;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Vector artwork shared by every category; never depends on installed font glyphs. */
public final class GuiIconArtwork {
    public enum Icon { MOON, COMBAT, MOVEMENT, DONUT, VISUALS, MISC, GUI, SETTINGS }

    public static BufferedImage render(Icon icon, int pixels) {
        BufferedImage image = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.scale(pixels / 24.0, pixels / 24.0);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (icon) {
            case MOON -> {
                Area moon = new Area(new Ellipse2D.Double(3, 3, 18, 18));
                moon.subtract(new Area(new Ellipse2D.Double(10, 0, 17, 18)));
                g.fill(moon);
            }
            case COMBAT -> {
                line(g, 14.5, 17.5, 3, 6, 3, 3, 6, 3, 17.5, 14.5);
                line(g, 13, 19, 19, 13);
                line(g, 16, 16, 20, 20);
                line(g, 19, 21, 21, 19);
                line(g, 14.5, 6.5, 18, 3, 21, 3, 21, 6, 17.5, 9.5);
                line(g, 5, 14, 9, 18);
                line(g, 7, 17, 4, 20);
                line(g, 3, 19, 5, 21);
            }
            case MOVEMENT -> {
                g.fill(new Ellipse2D.Double(12.5, 2, 4, 4));
                line(g, 5, 12, 8, 9, 12, 8, 15, 12, 20, 12);
                line(g, 12, 8, 11, 14, 16, 17, 16, 21);
                line(g, 11, 14, 8, 18, 3, 19);
                line(g, 2, 6, 7, 6);
            }
            case DONUT -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                line(g, 6, 8, 7, 7);
                line(g, 14, 6, 16, 7);
                line(g, 18, 11, 18, 13);
                line(g, 14, 18, 16, 17);
                line(g, 7, 17, 6, 15);
            }
            case VISUALS -> {
                Path2D eye = new Path2D.Double();
                eye.moveTo(2, 12);
                eye.quadTo(12, 0, 22, 12);
                eye.quadTo(12, 24, 2, 12);
                eye.closePath();
                g.draw(eye);
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
            }
            case MISC -> {
                for (int x : new int[]{4, 14}) {
                    for (int y : new int[]{4, 14}) {
                        g.draw(new RoundRectangle2D.Double(x, y, 6, 6, 1.8, 1.8));
                    }
                }
            }
            case GUI -> {
                g.draw(new RoundRectangle2D.Double(2.5, 3.5, 19, 17, 3, 3));
                line(g, 2.5, 9, 21.5, 9);
                line(g, 9, 9, 9, 20.5);
                line(g, 12.5, 13, 18, 13);
                line(g, 12.5, 17, 16, 17);
            }
            case SETTINGS -> {
                Path2D gear = new Path2D.Double();
                for (int i = 0; i < 32; i++) {
                    double angle = i * Math.PI / 16 - Math.PI / 2;
                    double radius = i % 4 == 1 || i % 4 == 2 ? 10 : 7.6;
                    double x = 12 + radius * Math.cos(angle), y = 12 + radius * Math.sin(angle);
                    if (i == 0) gear.moveTo(x, y); else gear.lineTo(x, y);
                }
                gear.closePath();
                g.draw(gear);
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
            }
        }
        g.dispose();
        return image;
    }

    private static void line(Graphics2D g, double... points) {
        Path2D path = new Path2D.Double();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        g.draw(path);
    }

    private GuiIconArtwork() {}
}
