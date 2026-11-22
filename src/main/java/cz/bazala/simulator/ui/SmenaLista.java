package cz.bazala.simulator.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Lišta průběhu denní směny — tenký panel zobrazený pod penězmi/XP barem.
 * Ukazuje: číslo směny, tržbu / cíl a progress bar.
 */
public class SmenaLista extends JPanel {

    private int cisloSmeny = 1;
    private int trzba      = 0;
    private int cil        = 1000;

    private static final Color BARVA_POZADI  = new Color(20, 20, 30, 210);
    private static final Color BARVA_FILL    = new Color(50, 160, 255, 220);
    private static final Color BARVA_FILL_OK = new Color(40, 200, 90, 220);
    private static final Color BARVA_TEXT    = new Color(230, 230, 230);
    private static final Font  FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 12);

    public SmenaLista() {
        setOpaque(false);
    }

    public void aktualizuj(int cisloSmeny, int trzba, int cil) {
        this.cisloSmeny = cisloSmeny;
        this.trzba      = trzba;
        this.cil        = cil > 0 ? cil : 1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int roh = 8;

        // Pozadí
        g2.setColor(BARVA_POZADI);
        g2.fillRoundRect(0, 0, w, h, roh, roh);

        // Progress bar — vnitřní oblast
        int padX = 6, padY = 4;
        int barW = w - 2 * padX;
        int barH = h - 2 * padY;
        int barRoh = 5;

        // Tmavý podklad baru
        g2.setColor(new Color(10, 10, 20, 180));
        g2.fillRoundRect(padX, padY, barW, barH, barRoh, barRoh);

        // Výplň podle pokroku
        double podil = Math.min(1.0, (double) trzba / cil);
        int fillW = (int)(barW * podil);
        boolean splneno = trzba >= cil;
        Color fillColor = splneno ? BARVA_FILL_OK : BARVA_FILL;

        if (fillW > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(padX, padY, fillW, barH, barRoh, barRoh);

            // Světlý pruh nahoře (highlight)
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRoundRect(padX, padY, fillW, barH / 2, barRoh, barRoh);
        }

        // Text: "Směna 2  •  1 250 / 2 000 Kč"
        g2.setFont(FONT_LABEL);
        FontMetrics fm = g2.getFontMetrics();
        String label = "Směna " + cisloSmeny + "  •  " + trzba + " / " + cil + " Kč";
        int tx = padX + 6;
        int ty = padY + (barH + fm.getAscent() - fm.getDescent()) / 2;

        // Stín textu
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(label, tx + 1, ty + 1);
        g2.setColor(BARVA_TEXT);
        g2.drawString(label, tx, ty);

        // Pokud splněno, přidej ✓ na pravou stranu
        if (splneno) {
            String check = "✓";
            int cx = padX + barW - fm.stringWidth(check) - 6;
            g2.setColor(new Color(100, 255, 130, 220));
            g2.drawString(check, cx, ty);
        }

        g2.dispose();
    }
}
