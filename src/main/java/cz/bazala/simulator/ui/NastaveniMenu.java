package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class NastaveniMenu extends JPanel {
    private Menu hlavniOkno;
    private JPanel sliderPanel;
    private int sliderHodnota = 100;
    private JComboBox<String> hudbaComboBox;
    private JComboBox<String> rozliseniComboBox;
    private BazalaSimulator simulator;
    private boolean nacitaniDat = false;

    // Barvy pro custom slider
    private static final Color SL_BG    = new Color(35, 35, 45, 220);
    private static final Color SL_FILL  = new Color(50, 130, 255);
    private static final Color SL_THUMB = new Color(255, 255, 255);
    private static final Color SL_TEXT  = new Color(200, 200, 215);

    public NastaveniMenu(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel nadpis = new JLabel("NASTAVENÍ");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 36));
        nadpis.setForeground(Color.WHITE);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(nadpis);
        mainContainer.add(Box.createVerticalStrut(20));

        // ── Hlasitost ──────────────────────────────────────────────────────
        addLabel(mainContainer, "Hlasitost hudby a zvuků:");
        mainContainer.add(Box.createVerticalStrut(6));
        mainContainer.add(vytvorSlider());
        mainContainer.add(Box.createVerticalStrut(16));

        // ── Hudba ──────────────────────────────────────────────────────────
        addLabel(mainContainer, "Hudba:");
        mainContainer.add(Box.createVerticalStrut(6));
        hudbaComboBox = vytvorComboBox();
        hudbaComboBox.addItem("Bez hudby");
        hudbaComboBox.addActionListener(e -> {
            if (nacitaniDat || hudbaComboBox.getSelectedItem() == null) return;
            String vybranaZobrazena = (String) hudbaComboBox.getSelectedItem();
            BazalaSimulator sim = hlavniOkno.getSimulator();
            if (sim == null) return;
            if (vybranaZobrazena.equals("Bez hudby")) {
                if (!"Bez hudby".equals(sim.getVybranaHudba())) {
                    sim.nastavVybranouHudbu("Bez hudby");
                    if (hlavniOkno.getPredchoziObrazovka().equals("BAZALA_SIMULATOR"))
                        SpravceZvuku.zastavVsechnuHudbu();
                }
            } else if (sim.getOdemcenaHudba() != null) {
                String puvodni = null;
                for (String h : sim.getOdemcenaHudba()) {
                    if (h.replace('_', ' ').equals(vybranaZobrazena)) { puvodni = h; break; }
                }
                if (puvodni != null && !puvodni.equals(sim.getVybranaHudba())) {
                    sim.nastavVybranouHudbu(puvodni);
                    if (hlavniOkno.getPredchoziObrazovka().equals("BAZALA_SIMULATOR")) {
                        SpravceZvuku.zastavVsechnuHudbu();
                        SpravceZvuku.prehraj(puvodni, puvodni, 0, true);
                    }
                }
            }
        });
        mainContainer.add(hudbaComboBox);
        mainContainer.add(Box.createVerticalStrut(16));

        // ── Rozlišení ──────────────────────────────────────────────────────
        addLabel(mainContainer, "Rozlišení:");
        mainContainer.add(Box.createVerticalStrut(6));
        rozliseniComboBox = vytvorComboBox();
        for (SpravceNastaveni.Zobrazeni z : SpravceNastaveni.Zobrazeni.values())
            rozliseniComboBox.addItem(z.popis);
        rozliseniComboBox.addActionListener(e -> {
            if (nacitaniDat) return;
            int idx = rozliseniComboBox.getSelectedIndex();
            if (idx < 0) return;
            SpravceNastaveni.Zobrazeni vybrane = SpravceNastaveni.Zobrazeni.values()[idx];
            if (vybrane == hlavniOkno.getAktualniZobrazeni()) return;
            SpravceNastaveni.setZobrazeni(vybrane);
            hlavniOkno.aplikujZobrazeni(vybrane);
        });
        mainContainer.add(rozliseniComboBox);
        mainContainer.add(Box.createVerticalStrut(24));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku(hlavniOkno.getPredchoziObrazovka()));
        mainContainer.add(btnZpet);

        bgPanel.add(mainContainer);
        add(bgPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) { obnovNabidkuHudby(); }
        });
    }

    // ── Pomocné továrny ────────────────────────────────────────────────────

    private void addLabel(JPanel parent, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(190, 190, 205));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(lbl);
    }

    /** Vlastní kreslený slider — bez výchozího Swing vzhledu. */
    private JPanel vytvorSlider() {
        sliderPanel = new JPanel() {
            private boolean tazeni = false;

            {
                setOpaque(false);
                setMaximumSize(new Dimension(300, 44));
                setPreferredSize(new Dimension(300, 44));
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) {
                        tazeni = true;
                        aktualizujHodnotu(e.getX());
                    SpravceNastaveni.setHlasitost(sliderHodnota);
                    }
                    @Override public void mouseReleased(MouseEvent e) { tazeni = false; }
                });
                addMouseMotionListener(new MouseAdapter() {
                    @Override public void mouseDragged(MouseEvent e) {
                        if (tazeni) aktualizujHodnotu(e.getX());
                    }
                });
            }

            private void aktualizujHodnotu(int mx) {
                int barX = 10, barW = getWidth() - 20;
                sliderHodnota = Math.max(0, Math.min(100, (int) Math.round((mx - barX) * 100.0 / barW)));
                SpravceZvuku.nastavHlasitost(sliderHodnota);
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int barH = 8, barY = h / 2 - barH / 2;
                int barX = 10, barW = w - 20;
                int fillW = (int) (barW * sliderHodnota / 100.0);
                int thumbX = barX + fillW;
                int thumbR = 10;

                // Pozadí drážky
                g2.setColor(new Color(30, 30, 40, 200));
                g2.fillRoundRect(barX, barY, barW, barH, barH, barH);

                // Výplň
                if (fillW > 0) {
                    g2.setColor(SL_FILL);
                    g2.fillRoundRect(barX, barY, fillW, barH, barH, barH);
                }

                // Palec (thumb)
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(thumbX - thumbR + 1, h / 2 - thumbR + 2, thumbR * 2, thumbR * 2);
                g2.setColor(SL_THUMB);
                g2.fillOval(thumbX - thumbR, h / 2 - thumbR, thumbR * 2, thumbR * 2);
                g2.setColor(SL_FILL);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(thumbX - thumbR, h / 2 - thumbR, thumbR * 2 - 1, thumbR * 2 - 1);

                // Hodnota
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(SL_TEXT);
                String val = sliderHodnota + " %";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, (w - fm.stringWidth(val)) / 2, barY - 4);

                g2.dispose();
            }
        };
        sliderPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return sliderPanel;
    }

    /** Vlastní ComboBox s tmavým vzhledem. */
    private JComboBox<String> vytvorComboBox() {
        JComboBox<String> cb = new JComboBox<String>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Pozadí
                g2.setPaint(new GradientPaint(0, 0, new Color(52, 52, 65), 0, h, new Color(33, 33, 44)));
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                // Lem
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                // Text vybrané položky
                Object sel = getSelectedItem();
                if (sel != null) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(sel.toString(), 10, (h + fm.getAscent() - fm.getDescent()) / 2);
                }
                // Šipka vpravo
                int ax = w - 20, ay = h / 2;
                g2.setColor(new Color(180, 180, 200));
                g2.fillPolygon(new int[]{ax, ax + 10, ax + 5}, new int[]{ay - 3, ay - 3, ay + 4}, 3);
                g2.dispose();
            }
        };
        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(null);
                b.setVisible(false);
                return b;
            }
            @Override protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override
                    protected void configureList() {
                        super.configureList();
                        list.setBackground(new Color(42, 42, 54));
                        list.setForeground(Color.WHITE);
                        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        list.setSelectionBackground(new Color(55, 110, 200));
                        list.setSelectionForeground(Color.WHITE);
                        list.setBorder(null);
                    }
                };
                popup.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 90), 1));
                return popup;
            }
        });
        cb.setOpaque(false);
        cb.setBorder(null);
        cb.setFocusable(false);
        cb.setMaximumSize(new Dimension(300, 36));
        cb.setPreferredSize(new Dimension(300, 36));
        cb.setAlignmentX(Component.CENTER_ALIGNMENT);
        return cb;
    }

    // ── Aktualizace stavu ─────────────────────────────────────────────────

    private void obnovNabidkuHudby() {
        nacitaniDat = true;
        // Nastav slider na uloženou hlasitost
        sliderHodnota = SpravceNastaveni.getHlasitost();
        if (sliderPanel != null) sliderPanel.repaint();
        hudbaComboBox.removeAllItems();
        hudbaComboBox.addItem("Bez hudby");

        simulator = hlavniOkno.getSimulator();
        if (simulator != null && simulator.getOdemcenaHudba() != null) {
            for (String h : simulator.getOdemcenaHudba())
                hudbaComboBox.addItem(h.replace('_', ' '));
            if (simulator.getVybranaHudba() != null) {
                if (simulator.getVybranaHudba().equals("Bez hudby"))
                    hudbaComboBox.setSelectedItem("Bez hudby");
                else
                    hudbaComboBox.setSelectedItem(simulator.getVybranaHudba().replace('_', ' '));
            }
        }
        if (rozliseniComboBox != null)
            rozliseniComboBox.setSelectedItem(hlavniOkno.getAktualniZobrazeni().popis);
        nacitaniDat = false;
    }
}
