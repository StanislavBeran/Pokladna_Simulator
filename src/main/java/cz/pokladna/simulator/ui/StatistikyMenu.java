package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.model.*;
import cz.pokladna.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;

public class StatistikyMenu extends JPanel {
    private final Menu hlavniOkno;
    private JPanel kartyContainer;

    public StatistikyMenu(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel nadpis = new JLabel("Statistiky a rekordy");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nadpis.setForeground(Color.WHITE);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(nadpis);
        mainContainer.add(Box.createVerticalStrut(16));

        kartyContainer = new JPanel();
        kartyContainer.setLayout(new BoxLayout(kartyContainer, BoxLayout.Y_AXIS));
        kartyContainer.setOpaque(false);
        kartyContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(kartyContainer);

        mainContainer.add(Box.createVerticalStrut(14));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));
        mainContainer.add(btnZpet);

        bgPanel.add(mainContainer);
        add(bgPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) { obnovKarty(); }
        });
        obnovKarty();
    }

    private void obnovKarty() {
        kartyContainer.removeAll();
        for (int slot = 1; slot <= 3; slot++) {
            UlozenaHra hra = SpravceSouboru.nactiUlozenouHru(slot);
            boolean prazdny = SpravceSouboru.STAV_PRAZDNY.equals(hra.nazevObchodu)
                    || SpravceSouboru.STAV_CHYBA.equals(hra.nazevObchodu);
            kartyContainer.add(vytvorKartu(slot, hra, prazdny));
            kartyContainer.add(Box.createVerticalStrut(10));
        }
        kartyContainer.revalidate();
        kartyContainer.repaint();
    }

    private JPanel vytvorKartu(int slot, UlozenaHra hra, boolean prazdny) {

        JPanel karta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int roh = 12;

                g2.setPaint(new GradientPaint(0, 0, new Color(52, 52, 62, 240), 0, h, new Color(32, 32, 42, 240)));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, roh, roh));

                Color akcent = prazdny ? new Color(80, 80, 90) : new Color(50, 130, 255);
                g2.setColor(akcent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 6, h, roh, roh));
                g2.fillRect(3, 0, 3, h);

                g2.setColor(new Color(255, 255, 255, 25));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, roh, roh));

                if (prazdny) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.setColor(new Color(130, 130, 140));
                    String txt = "Pozice " + slot + " — prázdná";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(txt, 20, h / 2 + fm.getAscent() / 2 - 2);
                } else {
                    int level = (hra.xp / 100) + 1;
                    int px = 20;
                    int py = 18;

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.setColor(new Color(80, 150, 255));
                    g2.drawString("SLOT " + slot, px, py);
                    py += 2;

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                    g2.setColor(Color.WHITE);
                    FontMetrics fmN = g2.getFontMetrics();
                    String nazev = hra.nazevObchodu;
                    int maxW = w - px - 110;
                    while (nazev.length() > 2 && fmN.stringWidth(nazev) > maxW) {
                        nazev = nazev.substring(0, nazev.length() - 2) + "…";
                    }
                    py += fmN.getAscent() + 2;
                    g2.drawString(nazev, px, py);

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g2.setColor(new Color(255, 213, 74));
                    String lvlTxt = "Level " + level;
                    FontMetrics fmL = g2.getFontMetrics();
                    g2.drawString(lvlTxt, w - fmL.stringWidth(lvlTxt) - 14, py);

                    py += 7;
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.drawLine(px, py, w - px, py);
                    py += 11;

                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    FontMetrics fmS = g2.getFontMetrics();
                    int radekV = fmS.getHeight() + 3;
                    int col2 = w / 2 + 10;

                    polozka(g2, fmS, "Směna", "#" + hra.cisloSmeny,         px,   py, col2 - px - 10);
                    polozka(g2, fmS, "Zákazníků", hra.celkemObslouzeno + "", col2, py, w - col2 - px);
                    py += radekV;
                    polozka(g2, fmS, "Nejlepší kombo", hra.nejlepsiKombo + "×",    px,   py, col2 - px - 10);
                    polozka(g2, fmS, "Rekord tržba", hra.nejlepsiTrzbaSmeny + " Kč", col2, py, w - col2 - px);
                    py += radekV;
                    polozka(g2, fmS, "Celkem vyděláno", hra.celkemVydelano + " Kč", px, py, w - px * 2);
                }

                g2.dispose();
            }

            private void polozka(Graphics2D g2, FontMetrics fm, String popis, String hodnota, int x, int y, int maxW) {
                g2.setColor(new Color(160, 160, 175));
                g2.drawString(popis + ": ", x, y);
                int offX = x + fm.stringWidth(popis + ": ");
                g2.setColor(Color.WHITE);
                String val = hodnota;
                while (val.length() > 1 && offX + fm.stringWidth(val) > x + maxW) {
                    val = val.substring(0, val.length() - 2) + "…";
                }
                g2.drawString(val, offX, y);
            }
        };

        karta.setOpaque(false);
        int kartaH = prazdny ? 56 : 128;
        karta.setMaximumSize(new Dimension(560, kartaH));
        karta.setPreferredSize(new Dimension(560, kartaH));
        return karta;
    }
}
