package cz.pokladna.simulator.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

class TenkyScrollBarUI extends BasicScrollBarUI {
    private final boolean svetly;

    TenkyScrollBarUI(boolean svetly) {
        this.svetly = svetly;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return nulovyButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return nulovyButton();
    }

    private JButton nulovyButton() {
        JButton b = new JButton();
        Dimension nula = new Dimension(0, 0);
        b.setPreferredSize(nula);
        b.setMinimumSize(nula);
        b.setMaximumSize(nula);
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        Graphics2D g2 = Vzhled.vyhlazene(g);
        g2.setColor(svetly ? new Color(0, 0, 0, 18) : new Color(255, 255, 255, 14));
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, r.width, r.width));
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = Vzhled.vyhlazene(g);
        Color barva = svetly
                ? (isDragging ? new Color(110, 112, 122) : new Color(160, 162, 170))
                : (isDragging ? new Color(150, 158, 178) : new Color(100, 108, 128));
        g2.setColor(barva);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, r.width, r.width));
        g2.dispose();
    }
}
