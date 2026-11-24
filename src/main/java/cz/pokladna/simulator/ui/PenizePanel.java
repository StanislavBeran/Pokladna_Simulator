package cz.pokladna.simulator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PenizePanel extends JPanel {

    private static final int DOBA_ZMENY_MS = 1400;

    private int cil = 0;
    private double zobrazeno = 0;
    private int zmena = 0;
    private long casZmeny = 0;
    private final Timer animace;

    public PenizePanel() {
        setOpaque(false);
        animace = new Timer(16, e -> krok());
    }

    public void setPenize(int penize) {
        if (!isShowing()) {
            cil = penize;
            zobrazeno = penize;
            zmena = 0;
            repaint();
            return;
        }
        int rozdil = penize - cil;
        if (rozdil == 0) return;
        zmena = rozdil;
        casZmeny = System.currentTimeMillis();
        cil = penize;
        if (!animace.isRunning()) animace.start();
    }

    private void krok() {
        double rozdil = cil - zobrazeno;
        if (Math.abs(rozdil) < 1) {
            zobrazeno = cil;
        } else {
            zobrazeno += rozdil * 0.18;
        }
        boolean zmenaDobehla = System.currentTimeMillis() - casZmeny > DOBA_ZMENY_MS;
        if (zobrazeno == cil && zmenaDobehla) {
            zmena = 0;
            animace.stop();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Vzhled.vyhlazene(g);
        int w = getWidth(), h = getHeight();

        g2.setColor(new Color(18, 21, 28, 225));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        g2.setColor(new Color(255, 255, 255, 30));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h));

        double d = h * 0.62;
        double odsazeni = (h - d) / 2;
        Vzhled.kresliMinci(g2, odsazeni + 2, odsazeni, d);

        Font font = new Font("Segoe UI", Font.BOLD, Math.max(10, (int) (h * 0.42)));
        FontMetrics fm = g2.getFontMetrics(font);
        int textX = (int) (odsazeni * 2 + d + h * 0.18);
        int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.setFont(font);
        g2.setColor(Vzhled.TEXT);
        g2.drawString(Vzhled.kc((int) Math.round(zobrazeno)), textX, textY);

        if (zmena != 0) {
            long uplynulo = System.currentTimeMillis() - casZmeny;
            float podil = Math.min(1f, uplynulo / (float) DOBA_ZMENY_MS);
            int alfa = (int) (255 * (1f - podil * podil));
            String text = (zmena > 0 ? "+" : "-") + Vzhled.kc(Math.abs(zmena)).replace("\u00A0Kč", "");
            Font fontZmeny = font.deriveFont(font.getSize2D() * 0.8f);
            FontMetrics fmZ = g2.getFontMetrics(fontZmeny);
            Color barva = zmena > 0 ? new Color(90, 220, 130) : new Color(240, 110, 110);
            g2.setFont(fontZmeny);
            g2.setColor(new Color(barva.getRed(), barva.getGreen(), barva.getBlue(), Math.max(0, alfa)));
            int posun = (int) (podil * h * 0.15);
            g2.drawString(text, w - fmZ.stringWidth(text) - (int) (h * 0.45), textY - posun);
        }
        g2.dispose();
    }
}
