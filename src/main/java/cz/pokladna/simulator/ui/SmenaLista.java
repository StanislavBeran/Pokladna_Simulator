package cz.pokladna.simulator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SmenaLista extends JPanel {

    private int cisloSmeny = 1;
    private int trzba = 0;
    private int cil = 1000;
    private double zobrazenyPodil = 0;
    private final Timer animace;

    public SmenaLista() {
        setOpaque(false);
        animace = new Timer(16, e -> krok());
    }

    public void aktualizuj(int cisloSmeny, int trzba, int cil) {
        boolean novaSmena = cisloSmeny != this.cisloSmeny;
        this.cisloSmeny = cisloSmeny;
        this.trzba = trzba;
        this.cil = cil > 0 ? cil : 1;
        if (novaSmena || !isShowing()) {
            zobrazenyPodil = cilovyPodil();
            repaint();
        } else if (!animace.isRunning()) {
            animace.start();
        }
    }

    private double cilovyPodil() {
        return Math.min(1.0, (double) trzba / cil);
    }

    private void krok() {
        double rozdil = cilovyPodil() - zobrazenyPodil;
        if (Math.abs(rozdil) < 0.002) {
            zobrazenyPodil = cilovyPodil();
            animace.stop();
        } else {
            zobrazenyPodil += rozdil * 0.12;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Vzhled.vyhlazene(g);
        int w = getWidth(), h = getHeight();
        boolean splneno = trzba >= cil;

        g2.setColor(new Color(18, 21, 28, 225));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        g2.setColor(new Color(255, 255, 255, 30));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h));

        int okraj = (int) (h * 0.16);
        int vnitrniH = h - 2 * okraj;

        Font fontOdznak = new Font("Segoe UI", Font.BOLD, Math.max(9, (int) (h * 0.3)));
        FontMetrics fmO = g2.getFontMetrics(fontOdznak);
        String odznak = "SMĚNA " + cisloSmeny;
        int odznakW = fmO.stringWidth(odznak) + (int) (h * 0.6);
        g2.setColor(splneno ? Vzhled.ZELENA : Vzhled.AKCENT);
        g2.fill(new RoundRectangle2D.Float(okraj, okraj, odznakW, vnitrniH, vnitrniH, vnitrniH));
        g2.setFont(fontOdznak);
        g2.setColor(Color.WHITE);
        g2.drawString(odznak, okraj + (odznakW - fmO.stringWidth(odznak)) / 2, (h + fmO.getAscent() - fmO.getDescent()) / 2);

        Font fontHodnota = new Font("Segoe UI", Font.BOLD, Math.max(9, (int) (h * 0.32)));
        FontMetrics fmH = g2.getFontMetrics(fontHodnota);
        String hodnota = splneno ? "Cíl splněn" : Vzhled.kc(trzba).replace("\u00A0Kč", "") + " / " + Vzhled.kc(cil);
        int hodnotaW = fmH.stringWidth(hodnota);
        int hodnotaX = w - okraj - (int) (h * 0.3) - hodnotaW;
        g2.setFont(fontHodnota);
        g2.setColor(splneno ? new Color(120, 230, 160) : Vzhled.TEXT);
        g2.drawString(hodnota, hodnotaX, (h + fmH.getAscent() - fmH.getDescent()) / 2);

        int barX = okraj + odznakW + (int) (h * 0.3);
        int barW = hodnotaX - (int) (h * 0.3) - barX;
        int barH = Math.max(6, (int) (h * 0.26));
        int barY = (h - barH) / 2;
        if (barW > 10) {
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, barH, barH));

            int fillW = (int) Math.round(barW * zobrazenyPodil);
            if (fillW > 0) {
                fillW = Math.max(fillW, barH);
                Color zacatek = splneno ? new Color(38, 166, 92) : new Color(58, 122, 232);
                Color konec = splneno ? new Color(110, 220, 150) : new Color(90, 200, 250);
                g2.setPaint(new GradientPaint(barX, 0, zacatek, barX + barW, 0, konec));
                g2.fill(new RoundRectangle2D.Float(barX, barY, fillW, barH, barH, barH));
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fill(new RoundRectangle2D.Float(barX, barY, fillW, barH / 2f, barH / 2f, barH / 2f));
            }
        }
        g2.dispose();
    }
}
