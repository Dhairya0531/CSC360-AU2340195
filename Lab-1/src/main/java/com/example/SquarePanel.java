package com.example;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

public class SquarePanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        int size = Math.min(w, h) * 2 / 3;
        int x = (w - size) / 2;
        int y = (h - size) / 2;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, size - 1, size - 1);
    }
}
