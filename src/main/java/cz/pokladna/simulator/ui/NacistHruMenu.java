package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.model.*;
import cz.pokladna.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class NacistHruMenu extends JPanel {
    private Menu hlavniOkno;
    private JPanel tlacitkaContainer;

    public NacistHruMenu(Menu okno) {
        this.hlavniOkno = okno;

        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel label = new JLabel("Uložené hry");
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        label.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainContainer.add(label, gbc);

        tlacitkaContainer = new JPanel(new GridBagLayout());
        tlacitkaContainer.setOpaque(false);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        mainContainer.add(tlacitkaContainer, gbc);

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainContainer.add(btnZpet, gbc);

        bgPanel.add(mainContainer);
        add(bgPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                vykresliAktualniTlacitka();
            }
        });

        vykresliAktualniTlacitka();
    }


    private void vykresliAktualniTlacitka() {
        tlacitkaContainer.removeAll();

        for (int i = 1; i <= 3; i++) {
            UlozenaHra hra = SpravceSouboru.nactiUlozenouHru(i);
            boolean jePrazdny = SpravceSouboru.STAV_PRAZDNY.equals(hra.nazevObchodu)
                    || SpravceSouboru.STAV_CHYBA.equals(hra.nazevObchodu);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i - 1;
            gbc.insets = new Insets(0, 0, 10, 0);
            tlacitkaContainer.add(vytvorKartuHry(i, hra, jePrazdny), gbc);
        }

        tlacitkaContainer.revalidate();
        tlacitkaContainer.repaint();
    }

    private JComponent vytvorKartuHry(int slot, UlozenaHra hra, boolean jePrazdny) {
        boolean[] stav = new boolean[2];
        JComponent karta = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                boolean hover = stav[0], stisk = stav[1];
                int w = getWidth(), h = getHeight();
                int posun = stisk ? 1 : 0;
                float roh = 16;

                if (jePrazdny) {
                    g2.setColor(new Color(34, 38, 48, 170));
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(1, 1, w - 2, h - 2, roh, roh));
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 6f}, 0f));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(1, 1, w - 2, h - 2, roh, roh));
                    kresliOdznakSlotu(g2, slot, 18, h, false);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.setColor(Vzhled.TEXT_TLUMENY);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Prázdná pozice", 74, (h + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                    return;
                }

                if (hover && !stisk) Vzhled.kresliStin(g2, 4, 2, w - 8, h - 8, (int) roh);
                Color pozadi = stisk ? new Color(40, 50, 72) : hover ? new Color(52, 62, 86) : new Color(34, 38, 48, 230);
                g2.setColor(pozadi);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(1, 1 + posun, w - 2, h - 2, roh, roh));
                g2.setColor(hover ? Vzhled.AKCENT : new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(hover ? 2f : 1f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(1, 1 + posun, w - 2, h - 2, roh, roh));

                kresliOdznakSlotu(g2, slot, 18, h + 2 * posun, true);

                int textX = 74;
                int pravyOkraj = w - (hover ? 64 : 22);
                Font fontNazev = new Font("Segoe UI", Font.BOLD, 17);
                Font fontDetail = new Font("Segoe UI", Font.PLAIN, 13);
                FontMetrics fmN = g2.getFontMetrics(fontNazev);
                FontMetrics fmD = g2.getFontMetrics(fontDetail);
                int y = (h - fmN.getHeight() - fmD.getHeight()) / 2 + fmN.getAscent() + posun;

                g2.setFont(fontNazev);
                g2.setColor(Vzhled.TEXT);
                g2.drawString(Vzhled.zkrat(hra.nazevObchodu, fmN, pravyOkraj - textX), textX, y);

                y += fmN.getDescent() + fmD.getAscent();
                g2.setFont(fontDetail);
                g2.setColor(Vzhled.TEXT_TLUMENY);
                String detail = "Směna " + hra.cisloSmeny + "   ·   " + Vzhled.kc(hra.pocetPenez);
                g2.drawString(Vzhled.zkrat(detail, fmD, pravyOkraj - textX), textX, y);

                if (hover) {
                    int d = 34;
                    int x = w - d - 18, cy = (h - d) / 2 + posun;
                    g2.setColor(Vzhled.AKCENT);
                    g2.fill(new java.awt.geom.Ellipse2D.Float(x, cy, d, d));
                    g2.setColor(Color.WHITE);
                    java.awt.geom.Path2D.Float sipka = new java.awt.geom.Path2D.Float();
                    sipka.moveTo(x + d * 0.4f, cy + d * 0.28f);
                    sipka.lineTo(x + d * 0.72f, cy + d * 0.5f);
                    sipka.lineTo(x + d * 0.4f, cy + d * 0.72f);
                    sipka.closePath();
                    g2.fill(sipka);
                }
                g2.dispose();
            }
        };
        karta.setOpaque(false);
        Dimension velikost = new Dimension(500, 78);
        karta.setPreferredSize(velikost);
        karta.setMinimumSize(velikost);
        karta.setCursor(jePrazdny ? Cursor.getDefaultCursor() : new Cursor(Cursor.HAND_CURSOR));

        if (!jePrazdny) {
            final int pozice = slot;
            karta.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    stav[0] = true;
                    karta.repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    stav[0] = false;
                    stav[1] = false;
                    karta.repaint();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    stav[1] = true;
                    karta.repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    stav[1] = false;
                    karta.repaint();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    stav[0] = false;
                    stav[1] = false;
                    PokladnaSimulator simulator = hlavniOkno.getSimulator();
                    simulator.nactiPenize(hra.pocetPenez);
                    simulator.nactiXp(hra.xp);
                    simulator.nactiVylepseni(hra.vylepseni);
                    simulator.nactiOdemceneZbozi(hra.odemceneZbozi);
                    simulator.nactiStatistiky(hra.celkemObslouzeno, hra.nejlepsiKombo,
                            hra.nejlepsiTrzbaSmeny, hra.celkemVydelano, hra.cisloSmeny,
                            hra.trzbaSmeny, hra.cilSmeny);
                    simulator.nactiStatistikySmeny(hra.zakazniciSmeny, hra.spokojeniSmeny,
                            hra.nastvaniSmeny, hra.chybySmeny, hra.nejlepsiKomboSmeny);
                    simulator.nastavDetailyHry(pozice, hra.nazevObchodu, hra.vybranaHudba, hra.odemcenaHudba);
                    SpravceZvuku.zastav("obchodak_theme_sound");
                    if (!"Bez hudby".equals(hra.vybranaHudba)) {
                        SpravceZvuku.prehraj("hra_hudba_v_pozadi", hra.vybranaHudba, 0, true);
                    }
                    hlavniOkno.zobrazObrazovku("POKLADNA_SIMULATOR");
                    simulator.spustSimulaci();
                }
            });
        }

        return karta;
    }

    private void kresliOdznakSlotu(Graphics2D g2, int slot, int x, int vyska, boolean aktivni) {
        int d = 40;
        int y = (vyska - d) / 2;
        g2.setColor(aktivni ? new Color(58, 122, 232, 60) : new Color(255, 255, 255, 18));
        g2.fill(new java.awt.geom.RoundRectangle2D.Float(x, y, d, d, 12, 12));
        Font f = new Font("Segoe UI", Font.BOLD, 18);
        FontMetrics fm = g2.getFontMetrics(f);
        String text = String.valueOf(slot);
        g2.setFont(f);
        g2.setColor(aktivni ? Vzhled.AKCENT_HOVER : Vzhled.TEXT_TLUMENY);
        g2.drawString(text, x + (d - fm.stringWidth(text)) / 2, y + (d + fm.getAscent() - fm.getDescent()) / 2);
    }
}