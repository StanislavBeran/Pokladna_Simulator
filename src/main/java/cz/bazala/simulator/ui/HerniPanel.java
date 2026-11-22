package cz.bazala.simulator.ui;

import javax.swing.*;
import java.awt.*;

public class HerniPanel extends JPanel {
    public enum Typ { PENIZE, XP }

    private Image bgImage;
    private final Typ typPanelu;

    private int penize = 0;
    private int xp = 0;
    private int maxXp = 100;
    private int level = 1;

    public HerniPanel(Typ typPanelu) {
        this.typPanelu = typPanelu;
        setOpaque(false);

        try {
            java.net.URL imgUrl = getClass().getResource("/UI.png");
            if (imgUrl != null) {
                bgImage = new ImageIcon(imgUrl).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPenize(int penize) {
        this.penize = penize;
        repaint();
    }

    public void setXpData(int level, int xp, int maxXp) {
        this.level = level;
        this.xp = xp;
        this.maxXp = maxXp;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();

        if (typPanelu == Typ.PENIZE) {
            String text = penize + " Kč";
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);

        } else if (typPanelu == Typ.XP) {
            int okrajX = 12;
            int okrajY = 15;
            int vnitrniSirka = getWidth() - (2 * okrajX);
            int vnitrniVyska = getHeight() - (2 * okrajY);
            int fillWidth = (int) (((double) xp / maxXp) * vnitrniSirka);

            g2.setColor(new Color(50, 150, 255, 180));
            g2.fillRoundRect(okrajX, okrajY, fillWidth, vnitrniVyska, 6, 6);

            String text = "Level " + level;
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);
        }
    }
}
