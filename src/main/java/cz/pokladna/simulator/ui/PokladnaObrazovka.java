package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.model.*;
import cz.pokladna.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class PokladnaObrazovka extends JPanel {

    private static final Color POZADI = new Color(22, 25, 32);
    private static final Color PANEL = new Color(34, 38, 48);
    private static final Color PANEL_SVETLY = new Color(48, 54, 68);
    private static final Color PANEL_HOVER = new Color(62, 70, 88);
    private static final Color AKCENT = new Color(58, 122, 232);
    private static final Color AKCENT_HOVER = new Color(84, 144, 248);
    private static final Color TEXT = new Color(232, 234, 240);
    private static final Color TEXT_TLUMENY = new Color(146, 152, 168);
    private static final Color PAPIR = new Color(250, 249, 244);
    private static final Color PAPIR_TEXT = new Color(40, 42, 48);
    private static final Color PAPIR_TLUMENY = new Color(130, 132, 140);

    private int aktualniKategorie;
    private JTextField numpadDisplay;
    private JTextField vyhledavac;
    private final List<Zbozi> zboziList;
    private final boolean isFs;
    private final Runnable onToggleFullscreen;
    private final PokladnaSimulator simulator;

    private final List<Zbozi> polozkyNaUctence;
    private SeznamPanel seznamZbozi;
    private SeznamPanel seznamUctenky;
    private JComponent hlavickaUctenky;
    private JComponent paticekUctenky;

    private boolean zobrazovatPlatbu = false;
    private int castkaVratit = 0;
    private int castkaVraceno = 0;
    private JLabel vahaLabel;

    public PokladnaObrazovka(boolean isFs, List<Zbozi> zboziList, Runnable onToggleFullscreen,
                             PokladnaSimulator simulator, int startKategorie, String startHledani, String startNumpad,
                             List<Zbozi> startUctenka) {
        this.isFs = isFs;
        this.zboziList = zboziList;
        this.onToggleFullscreen = onToggleFullscreen;
        this.simulator = simulator;
        this.aktualniKategorie = startKategorie;
        this.polozkyNaUctence = startUctenka != null ? new ArrayList<>(startUctenka) : new ArrayList<>();

        setLayout(new GridBagLayout());
        setBackground(POZADI);
        setOpaque(true);
        setFocusable(true);

        vytvorDesign(startHledani, startNumpad);
    }

    private int px(int maly, int velky) {
        return isFs ? velky : maly;
    }

    private void vytvorDesign(String startHledani, String startNumpad) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridy = 0;
        int mezera = px(2, 5);
        gbc.insets = new Insets(mezera, mezera, mezera, mezera);

        gbc.gridx = 0;
        gbc.weightx = isFs ? 0.45 : 0.46;
        add(sloupec(vytvorLevyPanel(startHledani)), gbc);

        gbc.gridx = 1;
        gbc.weightx = isFs ? 0.20 : 0.22;
        add(sloupec(vytvorNumpad(startNumpad)), gbc);

        gbc.gridx = 2;
        gbc.weightx = isFs ? 0.35 : 0.32;
        add(sloupec(vytvorPravyPanel()), gbc);
    }

    private JComponent sloupec(JComponent c) {
        c.setPreferredSize(new Dimension(0, 0));
        c.setMinimumSize(new Dimension(0, 0));
        return c;
    }

    // ── Levá část: kategorie, hledání, seznam zboží ────────────────────────

    private JComponent vytvorLevyPanel(String startHledani) {
        JPanel levy = new JPanel(new BorderLayout(0, px(3, 6)));
        levy.setOpaque(false);

        JPanel kategorie = new JPanel(new GridLayout(1, 4, px(2, 4), 0));
        kategorie.setOpaque(false);
        kategorie.add(vytvorZalozku("PEČIVO", 1));
        kategorie.add(vytvorZalozku("ZELENINA", 2));
        kategorie.add(vytvorZalozku("OVOCE", 3));
        kategorie.add(vytvorZalozku("OSTATNÍ", 4));
        kategorie.setPreferredSize(new Dimension(0, px(18, 40)));

        vyhledavac = new JTextField(startHledani);
        vyhledavac.setFont(new Font("Segoe UI", Font.PLAIN, px(11, 18)));
        vyhledavac.setForeground(TEXT);
        vyhledavac.setCaretColor(TEXT);
        vyhledavac.setOpaque(false);
        vyhledavac.setBorder(BorderFactory.createEmptyBorder(0, px(2, 4), 0, px(4, 8)));

        JLabel lupa = new JLabel("🔍");
        lupa.setFont(new Font("Segoe UI Emoji", Font.PLAIN, px(10, 18)));
        lupa.setForeground(TEXT_TLUMENY);
        lupa.setBorder(BorderFactory.createEmptyBorder(0, px(5, 10), 0, 0));

        JButton krizek = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                if (getModel().isRollover()) {
                    int d = Math.min(getWidth(), getHeight()) - px(4, 8);
                    g2.setColor(PANEL_HOVER);
                    g2.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        stylTlacitka(krizek, new Font("Segoe UI Symbol", Font.BOLD, px(9, 16)), TEXT_TLUMENY);
        krizek.setToolTipText("Zrušit hledání");
        krizek.setPreferredSize(new Dimension(px(18, 38), 0));
        krizek.setVisible(!vyhledavac.getText().isEmpty());
        krizek.addActionListener(e -> {
            SpravceZvuku.prehraj("kliknuti_na_obrazovku");
            vyhledavac.setText("");
            opustVyhledavani();
        });

        vyhledavac.addActionListener(e -> opustVyhledavani());
        vyhledavac.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "opustit_hledani");
        vyhledavac.getActionMap().put("opustit_hledani", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { opustVyhledavani(); }
        });
        vyhledavac.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void zmena() {
                boolean viditelny = !vyhledavac.getText().isEmpty();
                if (krizek.isVisible() != viditelny) {
                    krizek.setVisible(viditelny);
                    if (krizek.getParent() != null) krizek.getParent().revalidate();
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { zmena(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { zmena(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { zmena(); }
        });

        ZaoblenyPanel hledani = new ZaoblenyPanel(PANEL_SVETLY, px(6, 10));
        hledani.setLayout(new BorderLayout());
        hledani.add(lupa, BorderLayout.WEST);
        hledani.add(vyhledavac, BorderLayout.CENTER);
        hledani.add(krizek, BorderLayout.EAST);
        hledani.setPreferredSize(new Dimension(0, px(20, 42)));

        JPanel hlavicka = new JPanel(new BorderLayout(0, px(3, 6)));
        hlavicka.setOpaque(false);
        hlavicka.add(kategorie, BorderLayout.NORTH);
        hlavicka.add(hledani, BorderLayout.SOUTH);
        levy.add(hlavicka, BorderLayout.NORTH);

        seznamZbozi = new SeznamPanel(new GridLayout(0, 3, px(3, 8), px(3, 8)));
        seznamZbozi.setBorder(BorderFactory.createEmptyBorder(px(2, 4), px(2, 4), px(2, 4), px(2, 4)));
        vyhledavac.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { obnovSeznamZbozi(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { obnovSeznamZbozi(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { obnovSeznamZbozi(); }
        });
        obnovSeznamZbozi();

        ZaoblenyPanel ram = new ZaoblenyPanel(PANEL, px(8, 14));
        ram.setLayout(new BorderLayout());
        ram.setBorder(BorderFactory.createEmptyBorder(px(2, 4), px(2, 4), px(2, 4), px(2, 4)));
        ram.add(vytvorScroll(seznamZbozi, PANEL), BorderLayout.CENTER);
        levy.add(ram, BorderLayout.CENTER);
        return levy;
    }

    private JButton vytvorZalozku(String text, int idKategorie) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                boolean aktivni = aktualniKategorie == idKategorie;
                Color barva = aktivni ? AKCENT
                        : getModel().isPressed() ? PANEL
                        : getModel().isRollover() ? PANEL_HOVER : PANEL_SVETLY;
                g2.setColor(barva);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), px(6, 10), px(6, 10)));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        stylTlacitka(btn, new Font("Segoe UI", Font.BOLD, px(8, 15)), TEXT);
        btn.addActionListener(e -> {
            SpravceZvuku.prehraj("kliknuti_na_obrazovku");
            aktualniKategorie = (aktualniKategorie == idKategorie) ? TypZbozi.KATEGORIE_VSE : idKategorie;
            obnovSeznamZbozi();
            Container parent = btn.getParent();
            if (parent != null) parent.repaint();
        });
        return btn;
    }

    private JComponent vytvorDlazdici(Zbozi z) {
        int velikostObrazku = px(30, 76);
        Image obrazek = null;
        java.net.URL url = getClass().getResource("/zboziObrazky/" + z.nazev + ".png");
        if (url != null) {
            Image zmenseny = new ImageIcon(url).getImage().getScaledInstance(velikostObrazku, velikostObrazku, Image.SCALE_SMOOTH);
            obrazek = new ImageIcon(zmenseny).getImage();
        }
        final Image img = obrazek;
        final String nazev = (z.zkracenyNazev != null && !z.zkracenyNazev.isEmpty()) ? z.zkracenyNazev : z.nazev.replace('_', ' ');
        final String popisek = z.id + " · " + z.cena + " Kč";

        JComponent dlazdice = new JComponent() {
            private boolean nadNi = false;

            {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setToolTipText(z.nazev.replace('_', ' ') + " (ID " + z.id + ")");
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        SpravceZvuku.prehraj("kliknuti_na_obrazovku");
                        opustVyhledavani();
                        if (numpadDisplay != null) numpadDisplay.setText(String.valueOf(z.id));
                    }
                    @Override public void mouseEntered(MouseEvent e) { nadNi = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { nadNi = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                int w = getWidth(), h = getHeight();
                int roh = px(8, 14);

                g2.setColor(nadNi ? PANEL_HOVER : PANEL_SVETLY);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, roh, roh));
                if (nadNi) {
                    g2.setColor(AKCENT);
                    g2.setStroke(new BasicStroke(px(1, 2)));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, roh, roh));
                }

                Font fontNazev = new Font("Segoe UI", Font.BOLD, px(9, 16));
                Font fontPopisek = new Font("Segoe UI", Font.PLAIN, px(8, 13));
                FontMetrics fmN = g2.getFontMetrics(fontNazev);
                FontMetrics fmP = g2.getFontMetrics(fontPopisek);
                int textVyska = fmN.getHeight() + fmP.getHeight();
                int obrazekY = Math.max(px(2, 6), (h - textVyska - velikostObrazku) / 2);

                if (img != null) {
                    g2.drawImage(img, (w - velikostObrazku) / 2, obrazekY, null);
                }

                int y = h - px(3, 8) - fmN.getDescent();
                g2.setFont(fontNazev);
                g2.setColor(TEXT);
                String n = zkrat(nazev, fmN, w - px(4, 10));
                g2.drawString(n, (w - fmN.stringWidth(n)) / 2, y);

                y -= fmN.getHeight() - px(1, 2);
                g2.setFont(fontPopisek);
                g2.setColor(TEXT_TLUMENY);
                String p = zkrat(popisek, fmP, w - px(4, 10));
                g2.drawString(p, (w - fmP.stringWidth(p)) / 2, y);
                g2.dispose();
            }
        };
        dlazdice.setPreferredSize(new Dimension(0, px(58, 150)));
        return dlazdice;
    }

    // ── Střed: displej a číselník ──────────────────────────────────────────

    private JComponent vytvorNumpad(String startNumpad) {
        JPanel obal = new JPanel(new BorderLayout(0, px(3, 6)));
        obal.setOpaque(false);

        numpadDisplay = new JTextField(startNumpad);
        numpadDisplay.setEditable(false);
        numpadDisplay.setFocusable(false);
        numpadDisplay.setOpaque(false);
        numpadDisplay.setHorizontalAlignment(JTextField.RIGHT);
        numpadDisplay.setForeground(new Color(130, 245, 170));
        numpadDisplay.setFont(new Font("Consolas", Font.BOLD, px(16, 36)));
        numpadDisplay.setBorder(BorderFactory.createEmptyBorder(0, px(4, 10), 0, px(6, 12)));

        ZaoblenyPanel displej = new ZaoblenyPanel(new Color(12, 18, 16), px(6, 10));
        displej.setLayout(new BorderLayout());
        displej.add(numpadDisplay, BorderLayout.CENTER);
        displej.setPreferredSize(new Dimension(0, px(28, 64)));
        obal.add(displej, BorderLayout.NORTH);

        JPanel klavesnice = new JPanel(new GridLayout(5, 3, px(3, 6), px(3, 6)));
        klavesnice.setOpaque(false);
        String[] klavesy = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#", ",", "⌫", "↵"};
        for (String k : klavesy) {
            klavesnice.add(vytvorKlavesu(k));
        }
        obal.add(klavesnice, BorderLayout.CENTER);
        return obal;
    }

    private JButton vytvorKlavesu(String k) {
        boolean enter = k.equals("↵");
        boolean mazani = k.equals("⌫");
        Color zaklad = enter ? new Color(38, 166, 92) : mazani ? new Color(206, 72, 72) : PANEL_SVETLY;
        Color hover = enter ? new Color(54, 190, 110) : mazani ? new Color(228, 92, 92) : PANEL_HOVER;
        Color stisk = enter ? new Color(30, 140, 76) : mazani ? new Color(180, 58, 58) : PANEL;

        JButton btn = new JButton(k) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                Color barva = getModel().isPressed() ? stisk : getModel().isRollover() ? hover : zaklad;
                g2.setColor(barva);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), px(7, 12), px(7, 12)));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        String font = (enter || mazani) ? "Segoe UI Symbol" : "Segoe UI";
        stylTlacitka(btn, new Font(font, Font.BOLD, px(15, 32)), TEXT);
        btn.addActionListener(e -> {
            SpravceZvuku.prehraj("kliknuti_na_obrazovku");
            String aktualni = numpadDisplay.getText();
            if (mazani) {
                if (!aktualni.isEmpty()) numpadDisplay.setText(aktualni.substring(0, aktualni.length() - 1));
            } else if (enter) {
                zpracujVstupZNumpadu();
            } else {
                numpadDisplay.setText(aktualni + k);
            }
        });
        return btn;
    }

    // ── Pravá část: účtenka, váha, tlačítka ────────────────────────────────

    private JComponent vytvorPravyPanel() {
        JPanel pravy = new JPanel(new BorderLayout(0, px(3, 6)));
        pravy.setOpaque(false);

        ZaoblenyPanel papir = new ZaoblenyPanel(PAPIR, px(6, 10));
        papir.setLayout(new BorderLayout());
        papir.setBorder(BorderFactory.createEmptyBorder(px(3, 8), px(5, 12), px(3, 8), px(3, 8)));

        hlavickaUctenky = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(fontUctenky(Font.BOLD));
                return new Dimension(0, fm.getHeight() + px(5, 10));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                Font f = fontUctenky(Font.BOLD);
                FontMetrics fm = g2.getFontMetrics(f);
                g2.setFont(f);
                g2.setColor(PAPIR_TEXT);
                String jmeno = zkrat(simulator != null ? simulator.getJmenoObchodu() : "", fm, getWidth());
                g2.drawString(jmeno, (getWidth() - fm.stringWidth(jmeno)) / 2, fm.getAscent());
                prerusovanaCara(g2, getHeight() - px(2, 4), getWidth());
                g2.dispose();
            }
        };
        papir.add(hlavickaUctenky, BorderLayout.NORTH);

        seznamUctenky = new SeznamPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(fontUctenky(Font.PLAIN));
                return new Dimension(0, polozkyNaUctence.size() * vyskaRadku(fm) + px(2, 4));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                Font f = fontUctenky(Font.PLAIN);
                FontMetrics fm = g2.getFontMetrics(f);
                g2.setFont(f);
                int y = px(2, 4) + fm.getAscent();
                int w = getWidth();
                for (Zbozi z : polozkyNaUctence) {
                    String cena = cenaPolozky(z) + " Kč";
                    String mnozstvi = jeVazene(z) ? z.maxPocet + " g " : z.maxPocet + "× ";
                    int sirkaCeny = fm.stringWidth(cena);
                    String nazev = zkrat(mnozstvi + z.nazev.replace('_', ' '), fm, w - sirkaCeny - px(6, 12));

                    g2.setColor(PAPIR_TEXT);
                    g2.drawString(nazev, 0, y);
                    g2.drawString(cena, w - sirkaCeny, y);
                    y += vyskaRadku(fm);
                }
                g2.dispose();
            }
        };
        seznamUctenky.setOpaque(false);
        papir.add(vytvorScroll(seznamUctenky, PAPIR), BorderLayout.CENTER);

        paticekUctenky = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(fontUctenky(Font.BOLD));
                int radky = zobrazovatPlatbu ? 3 : 1;
                return new Dimension(0, px(5, 10) + radky * fm.getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                int w = getWidth();
                prerusovanaCara(g2, px(2, 4), w);

                Font tucny = fontUctenky(Font.BOLD);
                Font normal = fontUctenky(Font.PLAIN);
                FontMetrics fm = g2.getFontMetrics(tucny);
                int y = px(5, 10) + fm.getAscent();

                radekSouctu(g2, "CENA", getCelkovaCena() + " Kč", tucny, PAPIR_TEXT, y, w);
                if (zobrazovatPlatbu) {
                    y += fm.getHeight();
                    radekSouctu(g2, "Vrátit", castkaVratit + " Kč", normal, PAPIR_TLUMENY, y, w);
                    y += fm.getHeight();
                    Color barva = castkaVraceno == castkaVratit ? new Color(30, 140, 76) : AKCENT;
                    radekSouctu(g2, "Vráceno", castkaVraceno + " Kč", tucny, barva, y, w);
                }
                g2.dispose();
            }
        };
        papir.add(paticekUctenky, BorderLayout.SOUTH);
        pravy.add(papir, BorderLayout.CENTER);

        JPanel spodek = new JPanel(new BorderLayout(0, px(3, 6)));
        spodek.setOpaque(false);

        vahaLabel = new JLabel("Váha: -- g", SwingConstants.CENTER);
        vahaLabel.setFont(new Font("Segoe UI", Font.BOLD, px(10, 20)));
        vahaLabel.setForeground(TEXT);
        ZaoblenyPanel vaha = new ZaoblenyPanel(PANEL, px(6, 10));
        vaha.setLayout(new BorderLayout());
        vaha.add(vahaLabel, BorderLayout.CENTER);
        vaha.setPreferredSize(new Dimension(0, px(18, 40)));
        spodek.add(vaha, BorderLayout.NORTH);

        JPanel tlacitka = new JPanel(new GridLayout(1, isFs ? 1 : 2, px(3, 6), 0));
        tlacitka.setOpaque(false);
        tlacitka.setPreferredSize(new Dimension(0, px(26, 60)));
        if (!isFs) {
            JButton btnObchod = vytvorAkcniTlacitko("Obchod");
            btnObchod.addActionListener(e -> {
                if (simulator != null) simulator.zobrazObchod();
            });
            tlacitka.add(btnObchod);
        }
        JButton btnFullscreen = vytvorAkcniTlacitko(isFs ? "Zmenšit" : "Zvětšit");
        btnFullscreen.addActionListener(e -> onToggleFullscreen.run());
        tlacitka.add(btnFullscreen);
        spodek.add(tlacitka, BorderLayout.SOUTH);

        pravy.add(spodek, BorderLayout.SOUTH);
        return pravy;
    }

    private JButton vytvorAkcniTlacitko(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                Color barva = getModel().isPressed() ? new Color(44, 98, 196)
                        : getModel().isRollover() ? AKCENT_HOVER : AKCENT;
                g2.setColor(barva);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), px(7, 12), px(7, 12)));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        stylTlacitka(btn, new Font("Segoe UI", Font.BOLD, px(10, 20)), Color.WHITE);
        return btn;
    }

    private Font fontUctenky(int styl) {
        return new Font("Segoe UI", styl, px(9, 17));
    }

    private int vyskaRadku(FontMetrics fm) {
        return fm.getHeight() + px(1, 3);
    }

    private void radekSouctu(Graphics2D g2, String popis, String hodnota, Font font, Color barva, int y, int w) {
        FontMetrics fm = g2.getFontMetrics(font);
        g2.setFont(font);
        g2.setColor(barva);
        g2.drawString(popis, 0, y);
        g2.drawString(hodnota, w - fm.stringWidth(hodnota), y);
    }

    private void prerusovanaCara(Graphics2D g2, int y, int w) {
        g2.setColor(new Color(190, 190, 196));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
        g2.drawLine(0, y, w, y);
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Pomocné prvky vzhledu ──────────────────────────────────────────────

    private JScrollPane vytvorScroll(JComponent obsah, Color pozadi) {
        JScrollPane scroll = new JScrollPane(obsah);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(pozadi);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollBar lista = scroll.getVerticalScrollBar();
        lista.setUI(new TenkyScrollBarUI(pozadi == PAPIR));
        lista.setOpaque(false);
        lista.setUnitIncrement(px(16, 32));
        lista.setPreferredSize(new Dimension(px(6, 10), 0));
        return scroll;
    }

    private void stylTlacitka(JButton btn, Font font, Color barvaTextu) {
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFont(font);
        btn.setForeground(barvaTextu);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static Graphics2D vyhlazene(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static String zkrat(String text, FontMetrics fm, int maxSirka) {
        if (text == null) return "";
        if (fm.stringWidth(text) <= maxSirka) return text;
        String tecky = "…";
        int konec = text.length();
        while (konec > 0 && fm.stringWidth(text.substring(0, konec) + tecky) > maxSirka) konec--;
        return konec <= 0 ? "" : text.substring(0, konec).trim() + tecky;
    }

    private static class ZaoblenyPanel extends JPanel {
        private final Color barva;
        private final int roh;

        ZaoblenyPanel(Color barva, int roh) {
            this.barva = barva;
            this.roh = roh;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = vyhlazene(g);
            g2.setColor(barva);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), roh, roh));
            g2.dispose();
        }
    }

    private class SeznamPanel extends JPanel implements Scrollable {
        SeznamPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return px(16, 32); }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(1, r.height - px(16, 32)); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ── Logika (veřejné API zachováno) ─────────────────────────────────────

    private static boolean jeVazene(Zbozi z) {
        return z.minVaha > 0 && z.maxVaha > 0;
    }

    private static int cenaPolozky(Zbozi z) {
        return jeVazene(z) ? (int) Math.round((z.cena / 1000.0) * z.maxPocet) : z.cena * z.maxPocet;
    }

    private void obnovUctenku() {
        if (seznamUctenky == null) return;
        seznamUctenky.revalidate();
        seznamUctenky.repaint();
        paticekUctenky.revalidate();
        paticekUctenky.repaint();
        hlavickaUctenky.repaint();
        SwingUtilities.invokeLater(() ->
                seznamUctenky.scrollRectToVisible(new Rectangle(0, Math.max(0, seznamUctenky.getHeight() - 1), 1, 1)));
    }

    public int getAktualniKategorie() {
        return aktualniKategorie;
    }

    public String getHledanyText() {
        return vyhledavac != null ? vyhledavac.getText() : "";
    }

    public String getNumpadText() {
        return numpadDisplay != null ? numpadDisplay.getText() : "";
    }

    public List<Zbozi> getPolozkyNaUctence() {
        return polozkyNaUctence;
    }

    public void pridejPolozkuNaUctenku(Zbozi z, int mnozstvi) {
        polozkyNaUctence.add(z.kopieSMnozstvim(mnozstvi));
        obnovUctenku();
    }

    public void obnovSeznamZbozi() {
        if (seznamZbozi == null || vyhledavac == null) return;
        String hledany = vyhledavac.getText().toLowerCase();
        seznamZbozi.removeAll();
        for (Zbozi z : zboziList) {
            boolean odpovidaTextu = z.nazev.toLowerCase().contains(hledany) || String.valueOf(z.id).contains(hledany);
            boolean odpovidaKategorii = aktualniKategorie == TypZbozi.KATEGORIE_VSE || z.typ == aktualniKategorie;
            boolean odemceno = simulator.getOdemceneZbozi().contains(z.id);
            if (odpovidaTextu && odpovidaKategorii && odemceno && z.jeSkenovane()) {
                seznamZbozi.add(vytvorDlazdici(z));
            }
        }
        seznamZbozi.revalidate();
        seznamZbozi.repaint();
    }

    private void zpracujVstupZNumpadu() {
        String aktualniText = numpadDisplay.getText();
        if (!aktualniText.isEmpty()) {
            try {
                String[] casti = aktualniText.split("\\*");
                int hledaneId = Integer.parseInt(casti[0]);
                boolean zboziNalezeno = false;

                for (Zbozi z : zboziList) {
                    if (z.id == hledaneId) {
                        zboziNalezeno = true;
                        boolean pouzivaVahu = jeVazene(z);
                        if (pouzivaVahu && casti.length == 1) {
                            if (simulator != null) {
                                simulator.vypisDoKonzole("Chyba: Toto zboží se váží! Zadej ID*VÁHA (např. " + hledaneId + "*500).");
                            }
                            break;
                        }
                        int zadaneMnozstvi = (casti.length == 1) ? 1 : Integer.parseInt(casti[1]);

                        if (pouzivaVahu || z.maxPocet >= zadaneMnozstvi) {
                            boolean muzuPridat = true;
                            if (z.jeSkenovane() && simulator != null) {
                                muzuPridat = simulator.overZboziZeScanneru(hledaneId, zadaneMnozstvi);
                            }
                            if (muzuPridat) {
                                pridejPolozkuNaUctenku(z, zadaneMnozstvi);
                            } else if (simulator != null) {
                                simulator.vypisDoKonzole("Chyba: Zadaná váha/množství nesouhlasí se scannerem!");
                            }
                        } else if (simulator != null) {
                            simulator.vypisDoKonzole("Chyba: Zboží může být v max množství " + z.maxPocet + " ks!");
                        }
                        break;
                    }
                }

                if (!zboziNalezeno && simulator != null) {
                    simulator.vypisDoKonzole("Chyba: Neznámé ID zboží (" + hledaneId + ")!");
                }
            } catch (NumberFormatException ex) {
                if (simulator != null) {
                    simulator.vypisDoKonzole("Chyba: Neplatný vstup. Zadej ID zboží (např. 16) nebo ID*množství.");
                }
            }
        }
        numpadDisplay.setText("");
    }

    public int getCelkovaCena() {
        int celkem = 0;
        for (Zbozi z : polozkyNaUctence) celkem += cenaPolozky(z);
        return celkem;
    }

    public void vycistiUctenku() {
        polozkyNaUctence.clear();
        zobrazovatPlatbu = false;
        obnovUctenku();
    }

    public void nastavVahu(int vaha) {
        if (vahaLabel != null) {
            vahaLabel.setText(vaha > 0 ? "Váha: " + vaha + " g" : "Váha: -- g");
        }
    }

    public boolean piseDoVyhledavani() {
        return vyhledavac != null && vyhledavac.isFocusOwner();
    }

    private void opustVyhledavani() {
        if (piseDoVyhledavani()) requestFocusInWindow();
    }

    public void zpracujGlobalniKlavesu(char c) {
        if (numpadDisplay == null || piseDoVyhledavani()) return;
        if (c == '\n' || c == '\r') {
            SpravceZvuku.prehraj("kliknuti_na_obrazovku");
            zpracujVstupZNumpadu();
        } else if (c == '\b') {
            SpravceZvuku.prehraj("kliknuti_na_obrazovku");
            String t = numpadDisplay.getText();
            if (!t.isEmpty()) numpadDisplay.setText(t.substring(0, t.length() - 1));
        } else {
            char znak = (c == '.') ? ',' : c;
            if (Character.isDigit(znak) || znak == '*' || znak == '#' || znak == ',') {
                SpravceZvuku.prehraj("kliknuti_na_obrazovku");
                numpadDisplay.setText(numpadDisplay.getText() + znak);
            }
        }
    }

    public void nastavStavPlatby(boolean zobrazit, int vratit, int vraceno) {
        this.zobrazovatPlatbu = zobrazit;
        this.castkaVratit = vratit;
        this.castkaVraceno = vraceno;
        obnovUctenku();
    }
}
