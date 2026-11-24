package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

public class InteraktivniZona extends JPanel {

    private static final float KROK_NABEHU = 0.14f;
    private static final float KROK_ZABLESKU = 0.08f;

    private final int origX, origY, origW, origH;
    private final boolean jeKulaty;
    private final String nazev;
    private final PokladnaSimulator simulator;
    private final Timer animace;

    private boolean nadZonou = false;
    private float pruhlednost = 0f;
    private float zablesk = 0f;

    public String getNazev() {
        return nazev;
    }

    public InteraktivniZona(int x, int y, int w, int h, String nazev, PokladnaSimulator simulator) {
        this(x, y, w, h, nazev, false, simulator);
    }

    public InteraktivniZona(int x, int y, int w, int h, String nazev, boolean jeKulaty, PokladnaSimulator simulator) {
        this.origX = x;
        this.origY = y;
        this.origW = w;
        this.origH = h;
        this.nazev = nazev;
        this.jeKulaty = jeKulaty;
        this.simulator = simulator;

        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(nazev);

        animace = new Timer(16, e -> krok());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nadZonou = true;
                spustAnimaci();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nadZonou = false;
                spustAnimaci();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                zablesk = 1f;
                spustAnimaci();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (nazev.equals("vypínač")) {
                    if (simulator != null) simulator.prepniVypinac();
                } else {
                    if (simulator != null) simulator.zpracujKliknutiNaPolozku(nazev);

                    if (nazev.equals("Platební terminál")) {
                        SpravceZvuku.prehraj("pipnuti");
                    } else if (jeKulaty) {
                        SpravceZvuku.prehraj("mince");
                    } else {
                        SpravceZvuku.prehraj("bankovka");
                    }
                }
            }
        });
    }

    private void spustAnimaci() {
        if (!animace.isRunning()) animace.start();
    }

    private void krok() {
        float cil = nadZonou ? 1f : 0f;
        if (pruhlednost < cil) pruhlednost = Math.min(cil, pruhlednost + KROK_NABEHU);
        else if (pruhlednost > cil) pruhlednost = Math.max(cil, pruhlednost - KROK_NABEHU);
        zablesk = Math.max(0f, zablesk - KROK_ZABLESKU);

        if (pruhlednost == cil && zablesk == 0f) animace.stop();
        repaint();
    }

    public void aktualizujPozici(double scaleW, double scaleH) {
        setBounds((int) (origX * scaleW), (int) (origY * scaleH), (int) (origW * scaleW), (int) (origH * scaleH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (pruhlednost <= 0f && zablesk <= 0f) return;

        Graphics2D g2 = Vzhled.vyhlazene(g);
        int w = getWidth(), h = getHeight();
        float a = pruhlednost * (2f - pruhlednost);

        RectangularShape tvar = jeKulaty
                ? new Ellipse2D.Float(1, 1, w - 2, h - 2)
                : new RoundRectangle2D.Float(1, 1, w - 2, h - 2, Math.min(w, h) * 0.3f, Math.min(w, h) * 0.3f);

        int vypln = Math.min(255, (int) (50 * a + 110 * zablesk));
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, Math.min(255, vypln + 25)),
                0, h, new Color(255, 255, 255, Math.max(0, vypln - 15))));
        g2.fill(tvar);

        g2.setColor(new Color(255, 255, 255, Math.min(255, (int) (190 * a + 60 * zablesk))));
        g2.setStroke(new BasicStroke(1.6f));
        g2.draw(tvar);
        g2.dispose();
    }
}
