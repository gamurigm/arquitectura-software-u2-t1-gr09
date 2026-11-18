package com.mycompany.t1_consumo_api_pub_gr09.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;

public final class GlobePanel extends JPanel {
    public GlobePanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(168, 124));
        getAccessibleContext().setAccessibleName("Ilustración decorativa de un globo terráqueo");
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int size = Math.min(getHeight() - 12, getWidth() - 18);
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        g.setPaint(new GradientPaint(x, y, new Color(112, 183, 154), x + size, y + size, new Color(26, 92, 80)));
        g.fill(new Ellipse2D.Double(x, y, size, size));
        g.setColor(new Color(231, 247, 231, 190));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Double(x + size * .18, y, size * .64, size));
        g.draw(new Ellipse2D.Double(x + size * .37, y, size * .26, size));
        g.drawOval(x + 2, y + size / 3, size - 4, size / 3);
        g.drawOval(x + 5, y + size / 5, size - 10, size * 3 / 5);
        g.setColor(new Color(242, 248, 230, 120));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(x + 3, y + 3, size - 6, size - 6));
        g.dispose();
    }
}
