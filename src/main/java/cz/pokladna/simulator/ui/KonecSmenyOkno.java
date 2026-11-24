package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.model.SouhrnSmeny;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class KonecSmenyOkno extends JPanel {

    private static final int DOBA_ANIMACE_MS = 1100;
    private static final Font FONT_MALY = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_NADPIS = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_POPIS = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_HODNOTA = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_TLACITKO = new Font("Segoe UI", Font.BOLD, 15);

    private final JPanel okno;
    private final Timer animace;
    private long zacatekAnimace;
    private int hvezdy;
    private Runnable naPokracovani;

    public KonecSmenyOkno() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setVisible(false);
        addMouseListener(new MouseAdapter() {});

        okno = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth(), h = getHeight();
                Vzhled.kresliStin(g2, 8, 8, w - 16, h - 16, 24);
                g2.setColor(Vzhled.POZADI);
                g2.fill(new RoundRectangle2D.Float(8, 8, w - 16, h - 16, 24, 24));
                g2.setPaint(new GradientPaint(0, 8, new Color(255, 200, 40, 40), 0, 150, new Color(255, 200, 40, 0)));
                g2.fill(new RoundRectangle2D.Float(8, 8, w - 16, 150, 24, 24));
                g2.setColor(new Color(255, 255, 255, 22));
                g2.draw(new RoundRectangle2D.Float(8.5f, 8.5f, w - 17, h - 17, 24, 24));
                g2.dispose();
            }
        };
        okno.setOpaque(false);
        okno.setLayout(new BoxLayout(okno, BoxLayout.Y_AXIS));
        okno.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        add(okno);

        animace = new Timer(16, e -> {
            okno.repaint();
            if (System.currentTimeMillis() - zacatekAnimace > DOBA_ANIMACE_MS) ((Timer) e.getSource()).stop();
        });
    }

    public void zobraz(SouhrnSmeny s, List<String> noveZbozi, Runnable pokracovat, Runnable doObchodu) {
        this.hvezdy = s.hvezdy;
        this.naPokracovani = pokracovat;
        okno.removeAll();

        okno.add(stred(stitek("SMĚNA " + s.cisloSmeny + " DOKONČENA", FONT_MALY, Vzhled.ZLATA)));
        okno.add(Box.createVerticalStrut(10));
        okno.add(stred(vytvorHvezdy()));
        okno.add(Box.createVerticalStrut(8));
        okno.add(stred(stitek(nadpisHodnoceni(s.hvezdy), FONT_NADPIS, Vzhled.TEXT)));
        okno.add(Box.createVerticalStrut(2));
        okno.add(stred(stitek(popisHodnoceni(s), FONT_POPIS, Vzhled.TEXT_TLUMENY)));
        okno.add(Box.createVerticalStrut(20));

        JPanel statistiky = new JPanel(new GridLayout(2, 3, 10, 10));
        statistiky.setOpaque(false);
        statistiky.add(dlazdice("Tržba", Vzhled.kc(s.trzba), "cíl " + Vzhled.kc(s.cil), Vzhled.TEXT));
        statistiky.add(dlazdice("Zákazníci", String.valueOf(s.zakaznici), "obslouženo", Vzhled.TEXT));
        statistiky.add(dlazdice("Nejlepší kombo", "×" + s.nejlepsiKombo, "v řadě", Vzhled.TEXT));
        statistiky.add(dlazdice("Spokojení", String.valueOf(s.spokojeni), procento(s.spokojeni, s.zakaznici),
                new Color(110, 220, 150)));
        statistiky.add(dlazdice("Naštvaní", String.valueOf(s.nastvani), procento(s.nastvani, s.zakaznici),
                s.nastvani > 0 ? new Color(240, 120, 120) : Vzhled.TEXT));
        statistiky.add(dlazdice("Chyby", String.valueOf(s.chyby), "na pokladně",
                s.chyby > 0 ? new Color(240, 170, 90) : Vzhled.TEXT));
        statistiky.setMaximumSize(new Dimension(460, 166));
        statistiky.setPreferredSize(new Dimension(460, 166));
        okno.add(stred(statistiky));
        okno.add(Box.createVerticalStrut(14));

        okno.add(stred(vytvorBonus(s)));

        if (noveZbozi != null && !noveZbozi.isEmpty()) {
            okno.add(Box.createVerticalStrut(14));
            okno.add(stred(stitek("Nově k odemčení v obchodě", FONT_MALY, Vzhled.TEXT_TLUMENY)));
            okno.add(Box.createVerticalStrut(6));
            okno.add(stred(vytvorCipy(noveZbozi)));
        }

        okno.add(Box.createVerticalStrut(22));
        JPanel tlacitka = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        tlacitka.setOpaque(false);
        if (noveZbozi != null && !noveZbozi.isEmpty() && doObchodu != null) {
            JButton btnObchod = Vzhled.sekundarni("Do obchodu", FONT_TLACITKO);
            btnObchod.setPreferredSize(new Dimension(170, 46));
            btnObchod.addActionListener(e -> {
                zavri();
                doObchodu.run();
            });
            tlacitka.add(btnObchod);
        }
        JButton btnPokracovat = Vzhled.primarni("Začít směnu " + (s.cisloSmeny + 1), FONT_TLACITKO);
        btnPokracovat.putClientProperty(Vzhled.IKONA, "▶");
        btnPokracovat.setPreferredSize(new Dimension(220, 46));
        btnPokracovat.addActionListener(e -> {
            zavri();
            if (naPokracovani != null) naPokracovani.run();
        });
        tlacitka.add(btnPokracovat);
        okno.add(stred(tlacitka));

        setVisible(true);
        revalidate();
        repaint();
        zacatekAnimace = System.currentTimeMillis();
        animace.restart();
    }

    private void zavri() {
        animace.stop();
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(6, 8, 12, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    // ── Části okna ─────────────────────────────────────────────────────────

    private JComponent vytvorHvezdy() {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                long uplynulo = System.currentTimeMillis() - zacatekAnimace;
                int velikost = 46, mezera = 14;
                int celkem = 3 * velikost + 2 * mezera;
                int x0 = (getWidth() - celkem) / 2;
                for (int i = 0; i < 3; i++) {
                    double cx = x0 + i * (velikost + mezera) + velikost / 2.0;
                    double cy = getHeight() / 2.0 + (i == 1 ? -6 : 2);
                    boolean ziskana = i < hvezdy;
                    double meritko = 1.0;
                    if (ziskana) {
                        double t = (uplynulo - 200 - i * 220) / 260.0;
                        if (t <= 0) {
                            meritko = 0;
                        } else if (t < 1) {
                            meritko = 1 + Math.sin(t * Math.PI) * 0.35 - (1 - t) * 0.6;
                        }
                    }
                    kresliHvezdu(g2, cx, cy, velikost / 2.0, new Color(255, 255, 255, 25), 1.0);
                    if (ziskana && meritko > 0) {
                        kresliHvezdu(g2, cx, cy, velikost / 2.0 * meritko, Vzhled.ZLATA, meritko);
                    }
                }
                g2.dispose();
            }
        };
        Dimension d = new Dimension(200, 60);
        c.setPreferredSize(d);
        c.setMaximumSize(d);
        return c;
    }

    private void kresliHvezdu(Graphics2D g2, double cx, double cy, double r, Color barva, double meritko) {
        Path2D.Double hvezda = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double uhel = Math.PI / 2 + i * Math.PI / 5;
            double polomer = (i % 2 == 0) ? r : r * 0.45;
            double x = cx + Math.cos(uhel) * polomer;
            double y = cy - Math.sin(uhel) * polomer;
            if (i == 0) hvezda.moveTo(x, y);
            else hvezda.lineTo(x, y);
        }
        hvezda.closePath();
        if (barva == Vzhled.ZLATA) {
            g2.setColor(new Color(255, 200, 40, 60));
            g2.setStroke(new BasicStroke((float) (6 * meritko), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(hvezda);
            g2.setPaint(new GradientPaint((float) cx, (float) (cy - r), new Color(255, 232, 120),
                    (float) cx, (float) (cy + r), new Color(240, 160, 20)));
        } else {
            g2.setColor(barva);
        }
        g2.fill(hvezda);
    }

    private JComponent dlazdice(String popis, String hodnota, String poznamka, Color barvaHodnoty) {
        Vzhled.Karta karta = new Vzhled.Karta(Vzhled.PANEL, 14);
        karta.setLayout(new BoxLayout(karta, BoxLayout.Y_AXIS));
        karta.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        karta.add(vlevo(stitek(popis, FONT_POPIS, Vzhled.TEXT_TLUMENY)));
        karta.add(vlevo(stitek(hodnota, FONT_HODNOTA, barvaHodnoty)));
        karta.add(vlevo(stitek(poznamka, new Font("Segoe UI", Font.PLAIN, 11), new Color(110, 116, 132))));
        return karta;
    }

    private JComponent vytvorBonus(SouhrnSmeny s) {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(255, 200, 40, 26));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                g2.setColor(new Color(255, 200, 40, 90));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));

                Vzhled.kresliMinci(g2, 16, (h - 30) / 2.0, 30);

                g2.setFont(FONT_POPIS);
                FontMetrics fmP = g2.getFontMetrics();
                g2.setColor(Vzhled.TEXT_TLUMENY);
                String popis = s.nasobicBonusu > 1.0
                        ? "Bonus za směnu (×" + formatNasobic(s.nasobicBonusu) + " za hodnocení)"
                        : "Bonus za směnu";
                g2.drawString(popis, 58, (h + fmP.getAscent() - fmP.getDescent()) / 2);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fmH = g2.getFontMetrics();
                String hodnota = "+" + Vzhled.kc(s.bonus);
                g2.setColor(Vzhled.ZLATA);
                g2.drawString(hodnota, w - 16 - fmH.stringWidth(hodnota), (h + fmH.getAscent() - fmH.getDescent()) / 2);
                g2.dispose();
            }
        };
        Dimension d = new Dimension(460, 56);
        c.setPreferredSize(d);
        c.setMaximumSize(d);
        return c;
    }

    private JComponent vytvorCipy(List<String> polozky) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        panel.setOpaque(false);
        int zobrazit = Math.min(4, polozky.size());
        for (int i = 0; i < zobrazit; i++) panel.add(cip(polozky.get(i), Vzhled.PANEL_SVETLY));
        if (polozky.size() > zobrazit) panel.add(cip("+" + (polozky.size() - zobrazit) + " dalších", Vzhled.PANEL));
        panel.setMaximumSize(new Dimension(460, 70));
        return panel;
    }

    private JComponent cip(String text, Color barva) {
        Font f = new Font("Segoe UI", Font.BOLD, 12);
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(barva);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Vzhled.TEXT);
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        c.setPreferredSize(new Dimension(getFontMetrics(f).stringWidth(text) + 24, 26));
        return c;
    }

    // ── Pomocné ────────────────────────────────────────────────────────────

    private static JLabel stitek(String text, Font font, Color barva) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(barva);
        return l;
    }

    private static JComponent stred(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        return c;
    }

    private static JComponent vlevo(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private static String procento(int cast, int celek) {
        if (celek <= 0) return "—";
        return Math.round(100.0 * cast / celek) + " % zákazníků";
    }

    private static String formatNasobic(double n) {
        return n == Math.floor(n) ? String.valueOf((int) n) : String.valueOf(n).replace('.', ',');
    }

    private static String nadpisHodnoceni(int hvezdy) {
        switch (hvezdy) {
            case 3: return "Skvělá směna!";
            case 2: return "Dobrá práce";
            default: return "Směna zvládnuta";
        }
    }

    private static String popisHodnoceni(SouhrnSmeny s) {
        switch (s.hvezdy) {
            case 3: return "Spokojení zákazníci a skoro žádné chyby.";
            case 2: return "Pro tři hvězdy drž zákazníky spokojené a dělej méně chyb.";
            default: return "Zkus být rychlejší, ať zákazníci neodcházejí naštvaní.";
        }
    }
}
