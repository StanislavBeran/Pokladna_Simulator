package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class HerniMenu extends JPanel {

    private static final Font FONT_TLACITKO = new Font("Segoe UI", Font.BOLD, 15);
    private static final Dimension VELIKOST_TLACITKA = new Dimension(280, 46);

    private final PokladnaSimulator simulator;
    private final JLabel podnadpis;
    private final JButton btnReset;
    private final Timer zrusitPotvrzeni;
    private boolean cekaNaPotvrzeni = false;

    public HerniMenu(PokladnaSimulator simulator, Menu hlavniOkno) {
        this.simulator = simulator;

        setLayout(new GridBagLayout());
        setOpaque(false);
        addMouseListener(new MouseAdapter() {});

        JPanel okno = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int w = getWidth(), h = getHeight();
                Vzhled.kresliStin(g2, 8, 8, w - 16, h - 16, 24);
                g2.setColor(Vzhled.POZADI);
                g2.fill(new RoundRectangle2D.Float(8, 8, w - 16, h - 16, 24, 24));
                g2.setColor(new Color(255, 255, 255, 22));
                g2.draw(new RoundRectangle2D.Float(8.5f, 8.5f, w - 17, h - 17, 24, 24));
                g2.dispose();
            }
        };
        okno.setOpaque(false);
        okno.setLayout(new BoxLayout(okno, BoxLayout.Y_AXIS));
        okno.setBorder(BorderFactory.createEmptyBorder(34, 44, 30, 44));

        JComponent ikona = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Vzhled.vyhlazene(g);
                int d = Math.min(getWidth(), getHeight());
                float x = (getWidth() - d) / 2f;
                g2.setColor(new Color(58, 122, 232, 45));
                g2.fill(new Ellipse2D.Float(x, 0, d, d));
                g2.setColor(Vzhled.AKCENT_HOVER);
                float sirka = d * 0.12f, vyska = d * 0.4f;
                g2.fill(new RoundRectangle2D.Float(x + d * 0.36f, d * 0.3f, sirka, vyska, sirka, sirka));
                g2.fill(new RoundRectangle2D.Float(x + d * 0.52f, d * 0.3f, sirka, vyska, sirka, sirka));
                g2.dispose();
            }
        };
        Dimension velikostIkony = new Dimension(64, 64);
        ikona.setPreferredSize(velikostIkony);
        ikona.setMaximumSize(velikostIkony);
        ikona.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nadpis = new JLabel("Pauza");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 30));
        nadpis.setForeground(Vzhled.TEXT);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);

        podnadpis = new JLabel(" ");
        podnadpis.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        podnadpis.setForeground(Vzhled.TEXT_TLUMENY);
        podnadpis.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnPokracovat = vytvorTlacitko(Vzhled.primarni("Pokračovat", FONT_TLACITKO), "▶");
        btnPokracovat.addActionListener(e -> setVisible(false));

        JButton btnNastaveni = vytvorTlacitko(Vzhled.sekundarni("Nastavení", FONT_TLACITKO), "⚙");
        btnNastaveni.addActionListener(e -> {
            hlavniOkno.setPredchoziObrazovka("POKLADNA_SIMULATOR");
            hlavniOkno.zobrazObrazovku("NASTAVENI");
        });

        btnReset = vytvorTlacitko(Vzhled.tlacitko("Reset zákazníka", Vzhled.PANEL_SVETLY, Vzhled.PANEL_HOVER,
                Vzhled.PANEL, FONT_TLACITKO, Vzhled.TEXT), "↺");
        btnReset.addActionListener(e -> {
            if (cekaNaPotvrzeni) {
                nastavPotvrzeni(false);
                simulator.resetujZakaznika();
                setVisible(false);
            } else {
                nastavPotvrzeni(true);
            }
        });
        zrusitPotvrzeni = new Timer(3000, e -> nastavPotvrzeni(false));
        zrusitPotvrzeni.setRepeats(false);

        JButton btnUlozitOdejit = vytvorTlacitko(Vzhled.sekundarni("Uložit a odejít", FONT_TLACITKO), "⏏");
        btnUlozitOdejit.addActionListener(e -> {
            simulator.ulozHru();
            simulator.resetujHerniPole();
            setVisible(false);
            SpravceZvuku.zastavVsechnuHudbu();
            SpravceZvuku.prehraj("obchodak_theme_sound", "zvuk_v_pozadi", 0, true);
            hlavniOkno.zobrazObrazovku("HLAVNI_MENU");
        });

        JLabel napoveda = new JLabel("Esc = pokračovat ve hře");
        napoveda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        napoveda.setForeground(new Color(110, 116, 132));
        napoveda.setAlignmentX(Component.CENTER_ALIGNMENT);

        okno.add(ikona);
        okno.add(Box.createVerticalStrut(14));
        okno.add(nadpis);
        okno.add(Box.createVerticalStrut(2));
        okno.add(podnadpis);
        okno.add(Box.createVerticalStrut(24));
        okno.add(btnPokracovat);
        okno.add(Box.createVerticalStrut(10));
        okno.add(btnNastaveni);
        okno.add(Box.createVerticalStrut(10));
        okno.add(btnReset);
        okno.add(Box.createVerticalStrut(10));
        okno.add(btnUlozitOdejit);
        okno.add(Box.createVerticalStrut(18));
        okno.add(napoveda);
        add(okno);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "zavrit_pauzu");
        getActionMap().put("zavrit_pauzu", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { setVisible(false); }
        });
    }

    private JButton vytvorTlacitko(JButton btn, String ikona) {
        btn.putClientProperty(Vzhled.IKONA, ikona);
        btn.setPreferredSize(VELIKOST_TLACITKA);
        btn.setMaximumSize(VELIKOST_TLACITKA);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void nastavPotvrzeni(boolean potvrzeni) {
        cekaNaPotvrzeni = potvrzeni;
        if (potvrzeni) {
            btnReset.setText("Opravdu? Klikni znovu");
            btnReset.putClientProperty(Vzhled.IKONA, "⚠");
            btnReset.putClientProperty(Vzhled.BARVA, Vzhled.CERVENA);
            zrusitPotvrzeni.restart();
        } else {
            btnReset.setText("Reset zákazníka");
            btnReset.putClientProperty(Vzhled.IKONA, "↺");
            btnReset.putClientProperty(Vzhled.BARVA, null);
            if (zrusitPotvrzeni != null) zrusitPotvrzeni.stop();
        }
        btnReset.setToolTipText(potvrzeni ? "Rozdělaný nákup se zahodí, peníze zůstanou." : null);
        btnReset.repaint();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (btnReset != null) nastavPotvrzeni(false);
            if (podnadpis != null && simulator != null) podnadpis.setText(simulator.getJmenoObchodu());
        }
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
}
