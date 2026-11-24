package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.model.Rozmery;
import cz.pokladna.simulator.model.Zbozi;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class TutorialPruvodce extends JPanel {

    public enum Krok { SMENY, PENIZE, ZBOZI_NA_PASE, KOD, DELITKO, KARTA, DALSI_ZAKAZNIK, HOTOVE, KONEC, HOTOVO }

    public interface Cile {
        Rectangle smenaLista();
        Rectangle penize();
        Rectangle obrazovkaPokladny();
        Rectangle terminal();
        Rectangle platidla();
        Rectangle polozkaNaPase();
        Rectangle delitko();
        void nastartujPas();
    }

    private static final Font FONT_TITULEK = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color ZLATA = new Color(255, 200, 40);
    private static final int PADDING = 18;

    private final Cile cile;
    private final Runnable naKonec;
    private final JButton tlacitko;
    private final Timer animace;

    private Krok krok = Krok.HOTOVO;
    private String titulek = "";
    private String text = "";
    private List<String> radky = new ArrayList<>();
    private Rectangle bublina = new Rectangle();
    private int zahajenePlatby = 0;

    public TutorialPruvodce(Cile cile, Runnable naKonec) {
        this.cile = cile;
        this.naKonec = naKonec;

        setOpaque(false);
        setLayout(null);
        setVisible(false);

        tlacitko = Menu.vytvorTlacitko("Rozumím");
        tlacitko.setFocusable(false);
        tlacitko.setVisible(false);
        tlacitko.addActionListener(e -> potvrd());
        add(tlacitko);

        animace = new Timer(33, e -> repaint());
    }

    public void spust() {
        zahajenePlatby = 0;
        setVisible(true);
        animace.start();
        nastavKrok(Krok.SMENY);
    }

    public void zrus() {
        if (krok != Krok.HOTOVO) konec();
    }

    public boolean jeAktivni() {
        return krok != Krok.HOTOVO;
    }

    public boolean cekaNaPotvrzeni() {
        return krok == Krok.SMENY || krok == Krok.PENIZE || krok == Krok.KONEC;
    }

    public boolean pasSmiBezet() {
        return krok != Krok.SMENY && krok != Krok.PENIZE;
    }

    public void potvrd() {
        switch (krok) {
            case SMENY:
                nastavKrok(Krok.PENIZE);
                break;
            case PENIZE:
                nastavKrok(Krok.ZBOZI_NA_PASE);
                cile.nastartujPas();
                break;
            case KONEC:
                konec();
                break;
            default:
                break;
        }
    }

    public boolean jePrvniZakaznik() {
        return krok == Krok.ZBOZI_NA_PASE;
    }

    public Boolean vynucenaPlatbaKartou() {
        if (!jeAktivni()) return null;
        if (zahajenePlatby == 0) return Boolean.TRUE;
        if (zahajenePlatby == 1) return Boolean.FALSE;
        return null;
    }

    public void polozkaNaScanneru(Zbozi z, int pocetKusu, int vaha) {
        if (krok != Krok.ZBOZI_NA_PASE) return;
        String nazev = z.nazev.replace('_', ' ');
        String popis;
        if (vaha > 0) {
            popis = "Na scanneru je " + nazev + ". Toto zboží se váží: zadej na číselníku ID*VÁHA, např. "
                    + z.id + "*" + vaha + ". Váhu najdeš na obrazovce pokladny pod účtenkou. Potvrď Enterem.";
        } else {
            popis = "Na scanneru leží " + nazev + ". Na číselníku zadej kód zboží " + z.id
                    + " a počet kusů ve tvaru ID*POČET, např. " + z.id + "*3 jsou tři kusy. "
                    + "Počet spočítáš přímo na scanneru. Potvrď Enterem. Čísla můžeš psát i na klávesnici.";
        }
        nastavKrok(Krok.KOD, popis);
    }

    public void kodPotvrzen() {
        if (krok == Krok.KOD) nastavKrok(Krok.DELITKO);
    }

    public void platbaZahajena(boolean kartou, int castka, int vratit) {
        zahajenePlatby++;
        if (krok != Krok.DELITKO && krok != Krok.DALSI_ZAKAZNIK) return;
        if (kartou) {
            nastavKrok(Krok.KARTA, "Zákazník platí kartou částku " + castka
                    + " Kč. Klikni na platební terminál, který je zvýrazněný.");
        } else {
            nastavKrok(Krok.HOTOVE, "Zákazník platí hotově a dal ti víc, než nákup stojí. Vrať mu " + vratit
                    + " Kč: klikej na bankovky a mince u kasy, dokud součet přesně nesedí. "
                    + "Kolik už jsi vrátil, vidíš na účtence. Když vrátíš moc, začneš znovu.");
        }
    }

    public void platbaDokoncena() {
        if (krok == Krok.KARTA) nastavKrok(Krok.DALSI_ZAKAZNIK);
        else if (krok == Krok.HOTOVE) nastavKrok(Krok.KONEC);
    }

    public void zakaznikResetovan() {
        switch (krok) {
            case KOD:
            case DELITKO:
            case KARTA:
                zahajenePlatby = 0;
                nastavKrok(Krok.ZBOZI_NA_PASE);
                break;
            case HOTOVE:
                zahajenePlatby = 1;
                nastavKrok(Krok.DALSI_ZAKAZNIK);
                break;
            default:
                break;
        }
    }

    private void konec() {
        krok = Krok.HOTOVO;
        tlacitko.setVisible(false);
        animace.stop();
        setVisible(false);
        if (naKonec != null) naKonec.run();
    }

    private void nastavKrok(Krok novy) {
        nastavKrok(novy, null);
    }

    private void nastavKrok(Krok novy, String vlastniText) {
        krok = novy;
        switch (novy) {
            case SMENY:
                obsah("Směny",
                        "Hra se dělí na směny. Každá má cíl tržby, který ukazuje lišta nahoře. "
                                + "Až ho splníš, začne další, těžší směna a v obchodě se odemknou nové produkty.",
                        "Rozumím");
                break;
            case PENIZE:
                obsah("Peníze",
                        "Tady vidíš své peníze. Dostaneš je za obsloužené zákazníky. Utrácej je v Obchodě "
                                + "(tlačítko na obrazovce pokladny): koupíš nové zboží, vylepšení pokladny a hudbu.",
                        "Rozumím");
                break;
            case ZBOZI_NA_PASE:
                obsah("První zákazník",
                        "Na pás jede první zboží. Klikni na rohlík (nebo stiskni MEZERNÍK) "
                                + "a pošle se na scanner.",
                        null);
                break;
            case KOD:
                obsah("Zadej kód", vlastniText, null);
                break;
            case DELITKO:
                obsah("Konec nákupu",
                        "Zboží je odbavené. Až dojede dělítko nákupu, klikni na něj (nebo stiskni MEZERNÍK) "
                                + "a zákazník bude platit.",
                        null);
                break;
            case KARTA:
                obsah("Platba kartou", vlastniText, null);
                break;
            case DALSI_ZAKAZNIK:
                obsah("Výborně",
                        "Další zákazník, postup už znáš. Tip: MEZERNÍK posílá zboží na scanner "
                                + "a čísla s Enterem můžeš psát rovnou z klávesnice.",
                        null);
                break;
            case HOTOVE:
                obsah("Platba hotově", vlastniText, null);
                break;
            case KONEC:
                obsah("Hotovo",
                        "Teď už zvládáš celý postup. Plň cíl směny, vydělávej a nakupuj vylepšení. Hodně štěstí!",
                        "Jdeme na to");
                break;
            default:
                break;
        }
    }

    private void obsah(String novyTitulek, String novyText, String popisTlacitka) {
        titulek = novyTitulek;
        text = novyText;
        if (popisTlacitka != null) {
            tlacitko.setText(popisTlacitka);
            tlacitko.setVisible(true);
        } else {
            tlacitko.setVisible(false);
        }
        rozlozeni();
        repaint();
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (krok != Krok.HOTOVO) rozlozeni();
    }

    @Override
    public boolean contains(int x, int y) {
        return tlacitko.isVisible() && tlacitko.getBounds().contains(x, y);
    }

    private void rozlozeni() {
        double sx = Rozmery.scaleX(getWidth());
        double sy = Rozmery.scaleY(getHeight());
        int sirka = (int) Math.max(300, 350 * sx);

        FontMetrics fmTitulek = getFontMetrics(FONT_TITULEK);
        FontMetrics fmText = getFontMetrics(FONT_TEXT);
        radky = zalom(text == null ? "" : text, fmText, sirka - 2 * PADDING);

        int vyska = PADDING + fmTitulek.getHeight() + 8 + radky.size() * fmText.getHeight() + PADDING;
        if (tlacitko.isVisible()) vyska += 14 + 36;

        int x = (int) (20 * sx);
        int y = (int) (95 * sy);
        bublina = new Rectangle(x, y, sirka, vyska);

        if (tlacitko.isVisible()) {
            tlacitko.setBounds(x + sirka - PADDING - 150, y + vyska - PADDING - 36, 150, 36);
        }
    }

    private List<String> zalom(String vstup, FontMetrics fm, int maxSirka) {
        List<String> vysledek = new ArrayList<>();
        StringBuilder radek = new StringBuilder();
        for (String slovo : vstup.split(" ")) {
            String kandidat = radek.length() == 0 ? slovo : radek + " " + slovo;
            if (fm.stringWidth(kandidat) > maxSirka && radek.length() > 0) {
                vysledek.add(radek.toString());
                radek = new StringBuilder(slovo);
            } else {
                radek = new StringBuilder(kandidat);
            }
        }
        if (radek.length() > 0) vysledek.add(radek.toString());
        return vysledek;
    }

    private Rectangle cilKroku() {
        switch (krok) {
            case SMENY: return cile.smenaLista();
            case PENIZE: return cile.penize();
            case ZBOZI_NA_PASE: return cile.polozkaNaPase();
            case KOD: return cile.obrazovkaPokladny();
            case DELITKO: return cile.delitko();
            case KARTA: return cile.terminal();
            case HOTOVE: return cile.platidla();
            default: return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (krok == Krok.HOTOVO) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle cil = cilKroku();
        double puls = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 220.0);
        if (cil != null && cil.width > 0 && cil.x + cil.width > 0) {
            Rectangle zvyrazneni = new Rectangle(cil);
            zvyrazneni.grow(7, 7);
            kresliZvyrazneni(g2, zvyrazneni, puls);
            kresliSipku(g2, zvyrazneni, puls);
        }
        kresliBublinu(g2);
        g2.dispose();
    }

    private void kresliZvyrazneni(Graphics2D g2, Rectangle r, double puls) {
        RoundRectangle2D tvar = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 14, 14);
        g2.setColor(new Color(255, 200, 40, (int) (40 + 50 * puls)));
        g2.setStroke(new BasicStroke(9f));
        g2.draw(tvar);
        g2.setColor(new Color(255, 200, 40, (int) (170 + 85 * puls)));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(tvar);
    }

    private void kresliSipku(Graphics2D g2, Rectangle cil, double puls) {
        if (bublina.intersects(cil)) return;

        double zacatekX = Math.max(bublina.x, Math.min(bublina.x + bublina.width, cil.getCenterX()));
        double zacatekY = Math.max(bublina.y, Math.min(bublina.y + bublina.height, cil.getCenterY()));
        double konecX = Math.max(cil.x, Math.min(cil.x + cil.width, bublina.getCenterX()));
        double konecY = Math.max(cil.y, Math.min(cil.y + cil.height, bublina.getCenterY()));

        double dx = konecX - zacatekX;
        double dy = konecY - zacatekY;
        double delka = Math.hypot(dx, dy);
        if (delka < 20) return;

        double uhel = Math.atan2(dy, dx);
        double hlava = 14;
        double zakladnaX = konecX - Math.cos(uhel) * hlava * 0.8;
        double zakladnaY = konecY - Math.sin(uhel) * hlava * 0.8;

        g2.setColor(new Color(255, 200, 40, (int) (170 + 85 * puls)));
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) zacatekX, (int) zacatekY, (int) zakladnaX, (int) zakladnaY);

        Path2D.Double sipka = new Path2D.Double();
        sipka.moveTo(konecX, konecY);
        sipka.lineTo(konecX - Math.cos(uhel - 0.45) * hlava, konecY - Math.sin(uhel - 0.45) * hlava);
        sipka.lineTo(konecX - Math.cos(uhel + 0.45) * hlava, konecY - Math.sin(uhel + 0.45) * hlava);
        sipka.closePath();
        g2.fill(sipka);
    }

    private void kresliBublinu(Graphics2D g2) {
        Rectangle b = bublina;
        RoundRectangle2D tvar = new RoundRectangle2D.Float(b.x, b.y, b.width, b.height, 16, 16);

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fill(new RoundRectangle2D.Float(b.x + 4, b.y + 6, b.width, b.height, 16, 16));

        g2.setPaint(new GradientPaint(0, b.y, new Color(46, 50, 68, 245), 0, b.y + b.height, new Color(28, 30, 42, 245)));
        g2.fill(tvar);

        g2.setColor(new Color(255, 200, 40, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(tvar);

        FontMetrics fmTitulek = g2.getFontMetrics(FONT_TITULEK);
        FontMetrics fmText = g2.getFontMetrics(FONT_TEXT);

        g2.setFont(FONT_TITULEK);
        g2.setColor(ZLATA);
        g2.drawString(titulek, b.x + PADDING, b.y + PADDING + fmTitulek.getAscent());

        g2.setFont(FONT_TEXT);
        g2.setColor(new Color(225, 225, 235));
        int y = b.y + PADDING + fmTitulek.getHeight() + 8 + fmText.getAscent();
        for (String radek : radky) {
            g2.drawString(radek, b.x + PADDING, y);
            y += fmText.getHeight();
        }
    }
}
