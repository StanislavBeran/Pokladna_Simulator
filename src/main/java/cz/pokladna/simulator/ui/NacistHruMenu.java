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

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel label = new JLabel("Uložené hry:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(label);
        mainContainer.add(Box.createVerticalStrut(20));

        tlacitkaContainer = new JPanel();
        tlacitkaContainer.setLayout(new BoxLayout(tlacitkaContainer, BoxLayout.Y_AXIS));
        tlacitkaContainer.setOpaque(false);
        mainContainer.add(tlacitkaContainer);

        mainContainer.add(Box.createVerticalStrut(20));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));
        mainContainer.add(btnZpet);

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

            JPanel karta = vytvorKartuHry(i, hra, jePrazdny);
            karta.setAlignmentX(Component.CENTER_ALIGNMENT);
            tlacitkaContainer.add(karta);
            tlacitkaContainer.add(Box.createVerticalStrut(10));
        }

        tlacitkaContainer.revalidate();
        tlacitkaContainer.repaint();
    }

    private JPanel vytvorKartuHry(int slot, UlozenaHra hra, boolean jePrazdny) {
        JPanel karta = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(50, 50, 60, 220), 0, getHeight(), new Color(30, 30, 38, 220)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        karta.setOpaque(false);
        karta.setMaximumSize(new Dimension(480, 72));
        karta.setPreferredSize(new Dimension(480, 72));
        karta.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        karta.setCursor(jePrazdny ? Cursor.getDefaultCursor() : new Cursor(Cursor.HAND_CURSOR));

        if (jePrazdny) {
            JLabel lbl = new JLabel("Pozice " + slot + " — prázdná");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(new Color(140, 140, 140));
            karta.add(lbl, BorderLayout.CENTER);
        } else {
            int level = (hra.xp / 100) + 1;

            JPanel levy = new JPanel();
            levy.setLayout(new BoxLayout(levy, BoxLayout.Y_AXIS));
            levy.setOpaque(false);

            JLabel lblNazev = new JLabel(hra.nazevObchodu);
            lblNazev.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblNazev.setForeground(Color.WHITE);

            JLabel lblDetail = new JLabel("Směna " + hra.cisloSmeny + "  •  " + hra.pocetPenez + " Kč  •  Level " + level);
            lblDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblDetail.setForeground(new Color(180, 180, 200));

            levy.add(lblNazev);
            levy.add(Box.createVerticalStrut(4));
            levy.add(lblDetail);
            karta.add(levy, BorderLayout.CENTER);

            JLabel lblSlot = new JLabel("Slot " + slot);
            lblSlot.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSlot.setForeground(new Color(100, 160, 255));
            karta.add(lblSlot, BorderLayout.WEST);

            final int pozice = slot;
            karta.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    PokladnaSimulator simulator = hlavniOkno.getSimulator();
                    simulator.nactiPenize(hra.pocetPenez);
                    simulator.nactiXp(hra.xp);
                    simulator.nactiVylepseni(hra.vylepseni);
                    simulator.nactiOdemceneZbozi(hra.odemceneZbozi);
                    simulator.nactiStatistiky(hra.celkemObslouzeno, hra.nejlepsiKombo,
                            hra.nejlepsiTrzbaSmeny, hra.celkemVydelano, hra.cisloSmeny);
                    simulator.nastavDetailyHry(pozice, hra.nazevObchodu, hra.vybranaHudba, hra.odemcenaHudba);
                    SpravceZvuku.zastav("obchodak_theme_sound");
                    if (!"Bez hudby".equals(hra.vybranaHudba)) {
                        SpravceZvuku.prehraj("hra_hudba_v_pozadi", hra.vybranaHudba, 0, true);
                    }
                    hlavniOkno.zobrazObrazovku("POKLADNA_SIMULATOR");
                    simulator.spustSimulaci();
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    karta.setBackground(new Color(60, 80, 120, 220));
                    karta.repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    karta.repaint();
                }
            });
        }

        return karta;
    }


}