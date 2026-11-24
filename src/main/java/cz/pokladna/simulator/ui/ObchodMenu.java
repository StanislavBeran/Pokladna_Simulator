package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ObchodMenu extends JPanel {

    private static final Font FONT_NADPIS = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_PODNADPIS = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_ZALOZKA = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_NAZEV = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_MALY = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_TLACITKO = new Font("Segoe UI", Font.BOLD, 13);
    private static final int VYSKA_KARTY = 272;

    private static final String[][] HUDBA = {
            {"Tereza Kerndlová - Schody z nebe", "Tereza_Kerndlová_-_Schody_z_nebe", "500"},
            {"Athena Chlebová - Večerka", "Athena_Chlebová_-_Večerka", "1000"},
            {"Filip Dang - Pojď nakoupit bejbe", "Filip_Dang_-_Pojď_nakoupit_bejbe", "1500"},
            {"Filip Dang - Štědrá večerka", "Filip_Dang_-_Štědrá_večerka", "2500"},
            {"Gala - Freed from desire", "Gala_-_Freed_from_desire", "2000"},
            {"Michael Jackson - Billie Jean", "Michael_Jackson_-_Billie_Jean", "1000"},
            {"Filip Dang & Petr Pinkas - Čeká mě sleva", "Filip_Dang_&_Petr_Pinkas_-_Čeká_mě_sleva", "3000"}
    };

    private final PokladnaSimulator simulator;
    private int aktualniKategorie = 1;
    private final Vzhled.Seznam seznam;
    private final JComponent stitekPenez;
    private final JScrollPane scroll;
    private final List<JButton> zalozky = new ArrayList<>();

    public ObchodMenu(PokladnaSimulator simulator) {
        this.simulator = simulator;

        setLayout(new GridBagLayout());
        setOpaque(false);
        addMouseListener(new MouseAdapter() {});

        JPanel okno = new JPanel(new BorderLayout(0, 14)) {
            @Override
            public Dimension getPreferredSize() {
                Container rodic = getParent();
                int w = rodic != null && rodic.getWidth() > 0 ? rodic.getWidth() - 60 : 1000;
                int h = rodic != null && rodic.getHeight() > 0 ? rodic.getHeight() - 60 : 620;
                return new Dimension(Math.min(1000, w), Math.min(620, h));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth(), h = getHeight();
                Vzhled.kresliStin(g2, 8, 8, w - 16, h - 16, 22);
                g2.setColor(Vzhled.POZADI);
                g2.fill(new RoundRectangle2D.Float(8, 8, w - 16, h - 16, 22, 22));
                g2.setColor(new Color(255, 255, 255, 22));
                g2.draw(new RoundRectangle2D.Float(8.5f, 8.5f, w - 17, h - 17, 22, 22));
                g2.dispose();
            }
        };
        okno.setOpaque(false);
        okno.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        stitekPenez = vytvorStitekPenez();

        JButton zavrit = Vzhled.sekundarni("✕", new Font("Segoe UI Symbol", Font.BOLD, 15));
        zavrit.setToolTipText("Zavřít (Esc)");
        zavrit.setPreferredSize(new Dimension(38, 38));
        zavrit.addActionListener(e -> setVisible(false));

        JPanel vpravo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        vpravo.setOpaque(false);
        vpravo.add(stitekPenez);
        vpravo.add(zavrit);

        JLabel nadpis = new JLabel("Obchod");
        nadpis.setFont(FONT_NADPIS);
        nadpis.setForeground(Vzhled.TEXT);
        JLabel podnadpis = new JLabel("Nakupuj nové zboží, vylepšení pokladny a hudbu");
        podnadpis.setFont(FONT_PODNADPIS);
        podnadpis.setForeground(Vzhled.TEXT_TLUMENY);
        JPanel texty = new JPanel();
        texty.setLayout(new BoxLayout(texty, BoxLayout.Y_AXIS));
        texty.setOpaque(false);
        texty.add(nadpis);
        texty.add(podnadpis);

        JPanel hlavicka = new JPanel(new BorderLayout());
        hlavicka.setOpaque(false);
        hlavicka.add(texty, BorderLayout.WEST);
        hlavicka.add(vpravo, BorderLayout.EAST);

        JPanel radaZalozek = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radaZalozek.setOpaque(false);
        radaZalozek.add(vytvorZalozku("Zboží", 1));
        radaZalozek.add(vytvorZalozku("Vylepšení", 2));
        radaZalozek.add(vytvorZalozku("Hudba", 3));

        JPanel sever = new JPanel(new BorderLayout(0, 16));
        sever.setOpaque(false);
        sever.add(hlavicka, BorderLayout.NORTH);
        sever.add(radaZalozek, BorderLayout.SOUTH);
        okno.add(sever, BorderLayout.NORTH);

        seznam = new Vzhled.Seznam(new GridLayout(0, 4, 12, 12));
        seznam.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));
        scroll = Vzhled.scroll(seznam);
        okno.add(scroll, BorderLayout.CENTER);

        add(okno);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "zavrit_obchod");
        getActionMap().put("zavrit_obchod", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { setVisible(false); }
        });

        aktualizujNabidku();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (simulator != null) {
            if (visible) simulator.pozastavHru();
            else simulator.obnovHru();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(6, 8, 12, 190));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    // ── Hlavička ───────────────────────────────────────────────────────────

    private JComponent vytvorStitekPenez() {
        return new JComponent() {
            private final Font font = new Font("Segoe UI", Font.BOLD, 16);

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(font);
                return new Dimension(fm.stringWidth(Vzhled.kc(simulator.getAktualniPenize())) + 58, 38);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(Vzhled.PANEL_SVETLY);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                Vzhled.kresliMinci(g2, 9, (h - 22) / 2.0, 22);
                g2.setFont(font);
                g2.setColor(Vzhled.TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(Vzhled.kc(simulator.getAktualniPenize()), 40, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
    }

    private JButton vytvorZalozku(String text, int idKategorie) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                boolean aktivni = aktualniKategorie == idKategorie;
                int w = getWidth(), h = getHeight();
                g2.setColor(aktivni ? Vzhled.AKCENT : getModel().isRollover() ? Vzhled.PANEL_HOVER : Vzhled.PANEL);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(aktivni ? Color.WHITE : Vzhled.TEXT);
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setRolloverEnabled(true);
        btn.setFont(FONT_ZALOZKA);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 36));
        btn.addActionListener(e -> {
            if (aktualniKategorie == idKategorie) return;
            aktualniKategorie = idKategorie;
            aktualizujNabidku();
            for (JButton z : zalozky) z.repaint();
        });
        zalozky.add(btn);
        return btn;
    }

    // ── Nabídka ────────────────────────────────────────────────────────────

    public void aktualizujNabidku() {
        seznam.removeAll();
        seznam.setLayout(new GridLayout(0, 4, 12, 12));

        if (aktualniKategorie == 1) {
            List<Zbozi> zbozi = simulator.getZboziList();
            if (zbozi != null) {
                List<Zbozi> serazene = new ArrayList<>(zbozi);
                serazene.sort((a, b) -> Integer.compare(a.lvlOdemknuti, b.lvlOdemknuti));
                for (Zbozi z : serazene) seznam.add(kartaZbozi(z));
            }
        } else if (aktualniKategorie == 2) {
            for (StavHry.Vylepseni v : StavHry.Vylepseni.values()) seznam.add(kartaVylepseni(v));
        } else {
            for (String[] h : HUDBA) seznam.add(kartaHudby(h[0], h[1], Integer.parseInt(h[2])));
        }

        seznam.revalidate();
        seznam.repaint();
        stitekPenez.revalidate();
        stitekPenez.repaint();
    }

    private JComponent kartaZbozi(Zbozi z) {
        boolean vlastni = simulator.getOdemceneZbozi().contains(z.id);
        boolean zamceno = !vlastni && simulator.getCisloSmeny() < z.lvlOdemknuti;
        int cena = vypocitejCenuOdemknuti(z);
        boolean jeVazene = z.minVaha > 0 && z.maxVaha > 0;

        List<String[]> radky = new ArrayList<>();
        radky.add(new String[]{"Kategorie", TypZbozi.zKodu(z.typ).getNazev()});
        radky.add(jeVazene
                ? new String[]{"Váha", z.minVaha + "–" + z.maxVaha + " g"}
                : new String[]{"Max. kusů", String.valueOf(z.maxPocet)});
        radky.add(new String[]{"Prodej", Vzhled.kc(z.cena) + (jeVazene ? "/kg" : "/ks")});

        JButton akce = tlacitkoNakupu(vlastni, zamceno, z.lvlOdemknuti, cena, () -> {
            simulator.odectiPenize(cena);
            simulator.odemkniZbozi(z.id);
            aktualizujNabidku();
        });

        String odznak = vlastni ? "VLASTNÍŠ" : zamceno ? "OD SMĚNY " + z.lvlOdemknuti : null;
        Color barvaOdznaku = vlastni ? Vzhled.ZELENA : Vzhled.PANEL_HOVER;
        Image obrazek = nactiObrazek("/zboziObrazky/" + z.nazev + ".png", 72, !vlastni);
        return sestavKartu(obrazek, null, odznak, barvaOdznaku,
                z.nazev.replace('_', ' '), null, radky, null, -1, -1, akce, zamceno);
    }

    private JComponent kartaVylepseni(StavHry.Vylepseni v) {
        int uroven = simulator.getUrovenVylepseni(v);
        boolean jeMax = uroven >= v.maxUroven;
        int cena = cenaVylepseni(v, uroven);
        boolean prepinac = v.maxUroven == 1;

        JButton akce;
        if (jeMax) {
            akce = Vzhled.tlacitko(prepinac ? "Aktivní" : "Maximální úroveň", Vzhled.ZELENA, Vzhled.ZELENA, Vzhled.ZELENA, FONT_TLACITKO, Color.WHITE);
            akce.putClientProperty(Vzhled.VYPNUTA_BARVA, new Color(30, 92, 58));
            akce.putClientProperty(Vzhled.VYPNUTY_TEXT, new Color(170, 230, 190));
            akce.setEnabled(false);
        } else {
            akce = Vzhled.primarni((prepinac ? "Koupit · " : "Vylepšit · ") + Vzhled.kc(cena), FONT_TLACITKO);
            boolean maPenize = simulator.getAktualniPenize() >= cena;
            akce.setEnabled(maPenize);
            if (!maPenize) akce.putClientProperty(Vzhled.VYPNUTY_TEXT, new Color(232, 120, 120));
            akce.addActionListener(e -> {
                if (simulator.kupVylepseni(v, cena)) aktualizujNabidku();
            });
        }

        String odznak = jeMax ? (prepinac ? "ZAPNUTO" : "MAX") : null;
        JComponent karta = sestavKartu(null, ikonaVylepseni(v), odznak, Vzhled.ZELENA,
                nazevVylepseni(v), null, null, popisVylepseni(v),
                prepinac ? -1 : uroven, prepinac ? -1 : v.maxUroven, akce, false);
        karta.setPreferredSize(new Dimension(0, VYSKA_KARTY + 30));
        return karta;
    }

    private JComponent kartaHudby(String nazev, String soubor, int cena) {
        boolean vlastni = simulator.getOdemcenaHudba().contains(soubor);
        String[] casti = nazev.split(" - ", 2);
        String interpret = casti[0];
        String pisen = casti.length > 1 ? casti[1] : nazev;

        List<String[]> radky = new ArrayList<>();
        radky.add(new String[]{"Interpret", interpret});
        radky.add(new String[]{"Přehrát", "v Nastavení"});

        JButton akce = tlacitkoNakupu(vlastni, false, 0, cena, () -> {
            if (simulator.kupHudbu(soubor, cena)) aktualizujNabidku();
        });
        Image obrazek = nactiObrazek("/hudbaObrazky/" + soubor + ".png", 72, !vlastni);
        return sestavKartu(obrazek, null, vlastni ? "VLASTNÍŠ" : null, Vzhled.ZELENA,
                pisen, "Hudba", radky, null, -1, -1, akce, false);
    }

    private JButton tlacitkoNakupu(boolean vlastni, boolean zamceno, int smena, int cena, Runnable koupit) {
        JButton btn;
        if (vlastni) {
            btn = Vzhled.tlacitko("Vlastníš", Vzhled.ZELENA, Vzhled.ZELENA, Vzhled.ZELENA, FONT_TLACITKO, Color.WHITE);
            btn.putClientProperty(Vzhled.VYPNUTA_BARVA, new Color(30, 92, 58));
            btn.putClientProperty(Vzhled.VYPNUTY_TEXT, new Color(170, 230, 190));
            btn.setEnabled(false);
        } else if (zamceno) {
            btn = Vzhled.sekundarni("Odemkne se ve směně " + smena, FONT_TLACITKO);
            btn.setEnabled(false);
        } else {
            btn = Vzhled.primarni("Koupit · " + Vzhled.kc(cena), FONT_TLACITKO);
            boolean maPenize = simulator.getAktualniPenize() >= cena;
            btn.setEnabled(maPenize);
            if (!maPenize) btn.putClientProperty(Vzhled.VYPNUTY_TEXT, new Color(232, 120, 120));
            btn.addActionListener(e -> koupit.run());
        }
        return btn;
    }

    private JComponent sestavKartu(Image obrazek, String ikona, String odznak, Color barvaOdznaku,
                                   String nazev, String podtitul, List<String[]> radky, String popis,
                                   int uroven, int maxUroven, JButton akce, boolean ztlumena) {
        Vzhled.Karta karta = new Vzhled.Karta(Vzhled.PANEL, 16);
        karta.setLayout(new BorderLayout(0, 10));
        karta.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        karta.setPreferredSize(new Dimension(0, VYSKA_KARTY));

        JComponent nahled = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

                if (obrazek != null) {
                    g2.drawImage(obrazek, (w - obrazek.getWidth(null)) / 2, (h - obrazek.getHeight(null)) / 2, null);
                } else if (ikona != null) {
                    int d = Math.min(h - 12, 58);
                    g2.setColor(new Color(58, 122, 232, 45));
                    g2.fill(new Ellipse2D.Float((w - d) / 2f, (h - d) / 2f, d, d));
                    Font f = new Font("Segoe UI Symbol", Font.PLAIN, (int) (d * 0.52));
                    FontMetrics fm = g2.getFontMetrics(f);
                    g2.setFont(f);
                    g2.setColor(Vzhled.AKCENT_HOVER);
                    g2.drawString(ikona, (w - fm.stringWidth(ikona)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                }

                if (odznak != null) {
                    Font f = new Font("Segoe UI", Font.BOLD, 10);
                    FontMetrics fm = g2.getFontMetrics(f);
                    int bw = fm.stringWidth(odznak) + 14, bh = 20;
                    g2.setColor(barvaOdznaku);
                    g2.fill(new RoundRectangle2D.Float(w - bw - 6, 6, bw, bh, bh, bh));
                    g2.setFont(f);
                    g2.setColor(Color.WHITE);
                    g2.drawString(odznak, w - bw - 6 + 7, 6 + (bh + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        nahled.setPreferredSize(new Dimension(0, 90));
        karta.add(nahled, BorderLayout.NORTH);

        JComponent info = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth();
                FontMetrics fmN = g2.getFontMetrics(FONT_NAZEV);
                FontMetrics fmM = g2.getFontMetrics(FONT_MALY);
                int y = fmN.getAscent();

                g2.setFont(FONT_NAZEV);
                g2.setColor(ztlumena ? Vzhled.TEXT_TLUMENY : Vzhled.TEXT);
                g2.drawString(Vzhled.zkrat(nazev, fmN, w), 0, y);
                y += fmN.getDescent() + 2;

                g2.setFont(FONT_MALY);
                if (podtitul != null) {
                    y += fmM.getAscent();
                    g2.setColor(Vzhled.TEXT_TLUMENY);
                    g2.drawString(Vzhled.zkrat(podtitul, fmM, w), 0, y);
                    y += fmM.getDescent();
                }
                y += 6;

                if (radky != null) {
                    for (String[] r : radky) {
                        y += fmM.getAscent();
                        String hodnota = Vzhled.zkrat(r[1], fmM, w / 2 + 10);
                        g2.setColor(Vzhled.TEXT_TLUMENY);
                        g2.drawString(Vzhled.zkrat(r[0], fmM, w - fmM.stringWidth(hodnota) - 8), 0, y);
                        g2.setColor(ztlumena ? Vzhled.TEXT_TLUMENY : Vzhled.TEXT);
                        g2.drawString(hodnota, w - fmM.stringWidth(hodnota), y);
                        y += fmM.getDescent() + 4;
                    }
                }

                if (popis != null) {
                    g2.setColor(Vzhled.TEXT_TLUMENY);
                    for (String radek : Vzhled.zalom(popis, fmM, w)) {
                        y += fmM.getAscent();
                        g2.drawString(radek, 0, y);
                        y += fmM.getDescent() + 1;
                    }
                }

                if (maxUroven > 0) {
                    int d = 10, mezera = 6;
                    int py = getHeight() - d - 2;
                    for (int i = 0; i < maxUroven; i++) {
                        g2.setColor(i < uroven ? Vzhled.AKCENT : Vzhled.PANEL_HOVER);
                        g2.fill(new Ellipse2D.Float(i * (d + mezera), py, d, d));
                    }
                    g2.setFont(FONT_MALY);
                    g2.setColor(Vzhled.TEXT_TLUMENY);
                    String text = "Úroveň " + uroven + "/" + maxUroven;
                    g2.drawString(text, w - fmM.stringWidth(text), py + d - 1);
                }
                g2.dispose();
            }
        };
        karta.add(info, BorderLayout.CENTER);

        akce.setPreferredSize(new Dimension(0, 36));
        karta.add(akce, BorderLayout.SOUTH);

        Vzhled.sledujHover(karta, karta::setZvyraznena);
        return karta;
    }

    private Image nactiObrazek(String cesta, int velikost, boolean sedy) {
        java.net.URL url = getClass().getResource(cesta);
        if (url == null) return null;
        Image img = new ImageIcon(url).getImage().getScaledInstance(velikost, velikost, Image.SCALE_SMOOTH);
        if (sedy) img = GrayFilter.createDisabledImage(img);
        return new ImageIcon(img).getImage();
    }

    // ── Data vylepšení ─────────────────────────────────────────────────────

    private String ikonaVylepseni(StavHry.Vylepseni v) {
        switch (v) {
            case RYCHLOST_PASU: return "⚡";
            case BONUS_ODMEN: return "★";
            case TRPELIVOST: return "☺";
            case AUTO_SKEN_KUSOVE: return "⚙";
            default: return "✦";
        }
    }

    private String nazevVylepseni(StavHry.Vylepseni v) {
        switch (v) {
            case RYCHLOST_PASU: return "Nový motor pásu";
            case BONUS_ODMEN: return "Věrnostní program";
            case TRPELIVOST: return "Příjemné prostředí";
            case AUTO_SKEN_KUSOVE: return "Chytré kódy";
            default: return v.name();
        }
    }

    private String popisVylepseni(StavHry.Vylepseni v) {
        switch (v) {
            case RYCHLOST_PASU: return "Zrychlí pás, zkrátí čekání na další zboží a urychlí úklid.";
            case BONUS_ODMEN: return "Víc peněz za každý nákup (+15 % za úroveň).";
            case TRPELIVOST: return "Zákazníci vydrží déle v klidu (+30 % trpělivosti za úroveň).";
            case AUTO_SKEN_KUSOVE: return "Zboží s čárovým kódem se odbaví samo, bez klikání.";
            default: return "";
        }
    }

    private int cenaVylepseni(StavHry.Vylepseni v, int uroven) {
        switch (v) {
            case RYCHLOST_PASU: return 5000 * (uroven + 1);
            case BONUS_ODMEN: return 4000 * (uroven + 1);
            case TRPELIVOST: return 3500 * (uroven + 1);
            case AUTO_SKEN_KUSOVE: return 10000;
            default: return 5000 * (uroven + 1);
        }
    }

    private int vypocitejCenuOdemknuti(Zbozi z) {
        int zaklad = z.cena * z.maxPocet * 3;
        if (z.minVaha > 0 && z.maxVaha > 0) {
            zaklad += 1000;
        }
        zaklad += z.typ * 200;
        zaklad += z.lvlOdemknuti * 1200;
        return Math.max(100, (zaklad / 50) * 25);
    }
}
