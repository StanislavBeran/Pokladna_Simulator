package cz.pokladna.simulator.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleSupplier;

public class ZakaznikPanel extends JPanel {

    static final double POMER_STRAN = 2240.0 / 1910.0;

    private static final double SIRKA_UI = 2240, VYSKA_UI = 1910;
    private static final Rectangle BOX_CHAT = new Rectangle(109, 239, 935, 1485);
    private static final Rectangle BOX_SPOKOJENOST = new Rectangle(1293, 236, 760, 150);
    private static final Rectangle BOX_FOTKA = new Rectangle(1293, 478, 761, 935);
    private static final Rectangle BOX_NALADA = new Rectangle(1210, 1516, 925, 296);
    private static final Color BARVA_TEXTU = new Color(170, 30, 36);
    private static final float KROK_ANIMACE = 0.08f;
    private static final int MAX_ZAKAZNIKU = 50;
    private static final int MAX_ZPRAV = 30;

    private enum Faze { PRAZDNO, PRICHOD, STOJI, ODCHOD }

    private static final class Zprava {
        final String text;
        final boolean odZakaznika;

        Zprava(String text, boolean odZakaznika) {
            this.text = text;
            this.odZakaznika = odZakaznika;
        }
    }

    private final List<Zprava> chat = new ArrayList<>();

    private final DoubleSupplier zdrojSpokojenosti;
    private final BufferedImage ramec;
    private final List<BufferedImage> zakaznici = new ArrayList<>();
    private final Random nahoda = new Random();
    private final Timer animace;

    private BufferedImage ramecZmenseny;
    private int aktualni = -1;
    private Faze faze = Faze.PRAZDNO;
    private float prechod = 0f;
    private boolean cekaDalsi = false;
    private double zobrazenaSpokojenost = 1.0;

    public ZakaznikPanel(DoubleSupplier zdrojSpokojenosti) {
        this.zdrojSpokojenosti = zdrojSpokojenosti;
        setOpaque(false);
        ramec = nacti("/zakaznikUI.png");
        for (int i = 1; i <= MAX_ZAKAZNIKU; i++) {
            BufferedImage img = nacti("/zakaznici/zakaznik" + i + ".png");
            if (img == null) break;
            zakaznici.add(img);
        }
        animace = new Timer(16, e -> krok());
        animace.start();
    }

    private static BufferedImage nacti(String cesta) {
        URL url = ZakaznikPanel.class.getResource(cesta);
        if (url == null) return null;
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            System.err.println("Nepodařilo se načíst obrázek " + cesta + ": " + e.getMessage());
            return null;
        }
    }

    public void pridejZpravuZakaznika(String text) {
        pridejZpravu(text, true);
    }

    public void pridejZpravuPokladniho(String text) {
        pridejZpravu(text, false);
    }

    private void pridejZpravu(String text, boolean odZakaznika) {
        if (text == null || text.isBlank()) return;
        chat.add(new Zprava(text.trim(), odZakaznika));
        while (chat.size() > MAX_ZPRAV) chat.remove(0);
        repaint();
    }

    public void vycistiChat() {
        chat.clear();
        repaint();
    }

    public void novyZakaznik() {
        chat.clear();
        repaint();
        if (zakaznici.isEmpty()) return;
        if (faze == Faze.PRAZDNO) {
            prijdeDalsi();
        } else {
            cekaDalsi = true;
            faze = Faze.ODCHOD;
        }
    }

    public void vycisti() {
        faze = Faze.PRAZDNO;
        prechod = 0f;
        cekaDalsi = false;
        aktualni = -1;
        zobrazenaSpokojenost = 1.0;
        chat.clear();
        repaint();
    }

    private void prijdeDalsi() {
        int novy;
        if (zakaznici.size() == 1) {
            novy = 0;
        } else {
            do {
                novy = nahoda.nextInt(zakaznici.size());
            } while (novy == aktualni);
        }
        aktualni = novy;
        prechod = 0f;
        faze = Faze.PRICHOD;
        zobrazenaSpokojenost = 1.0;
    }

    private void krok() {
        if (!isShowing()) return;
        boolean zmena = false;

        if (faze == Faze.PRICHOD) {
            prechod = Math.min(1f, prechod + KROK_ANIMACE);
            if (prechod >= 1f) faze = Faze.STOJI;
            zmena = true;
        } else if (faze == Faze.ODCHOD) {
            prechod = Math.max(0f, prechod - KROK_ANIMACE);
            if (prechod <= 0f) {
                if (cekaDalsi) {
                    cekaDalsi = false;
                    prijdeDalsi();
                } else {
                    faze = Faze.PRAZDNO;
                }
            }
            zmena = true;
        }

        double cil = (faze == Faze.PRAZDNO) ? 1.0 : zdrojSpokojenosti.getAsDouble();
        double rozdil = cil - zobrazenaSpokojenost;
        if (Math.abs(rozdil) > 0.0005) {
            zobrazenaSpokojenost += rozdil * 0.2;
            zmena = true;
        }
        if (zmena) repaint();
    }

    private Rectangle naObrazovku(Rectangle r) {
        double sx = getWidth() / SIRKA_UI, sy = getHeight() / VYSKA_UI;
        return new Rectangle((int) Math.round(r.x * sx), (int) Math.round(r.y * sy),
                (int) Math.round(r.width * sx), (int) Math.round(r.height * sy));
    }

    private BufferedImage ramecVeVelikosti(int w, int h) {
        if (ramec == null || w <= 0 || h <= 0) return null;
        if (ramecZmenseny == null || ramecZmenseny.getWidth() != w || ramecZmenseny.getHeight() != h) {
            ramecZmenseny = zmensi(ramec, w, h);
        }
        return ramecZmenseny;
    }

    private static BufferedImage zmensi(BufferedImage zdroj, int w, int h) {
        BufferedImage aktualniObr = zdroj;
        int cw = zdroj.getWidth(), ch = zdroj.getHeight();
        while (cw / 2 >= w && ch / 2 >= h) {
            cw /= 2;
            ch /= 2;
            aktualniObr = vykresli(aktualniObr, cw, ch);
        }
        return vykresli(aktualniObr, w, h);
    }

    private static BufferedImage vykresli(BufferedImage zdroj, int w, int h) {
        BufferedImage cil = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cil.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(zdroj, 0, 0, w, h, null);
        g.dispose();
        return cil;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Vzhled.vyhlazene(g);
        int w = getWidth(), h = getHeight();

        BufferedImage pozadi = ramecVeVelikosti(w, h);
        if (pozadi != null) g2.drawImage(pozadi, 0, 0, null);

        kresliFotku(g2, naObrazovku(BOX_FOTKA));
        kresliSpokojenost(g2, naObrazovku(BOX_SPOKOJENOST));
        kresliNaladu(g2, naObrazovku(BOX_NALADA));
        kresliChat(g2, naObrazovku(BOX_CHAT));
        g2.dispose();
    }

    private void kresliChat(Graphics2D g2, Rectangle r) {
        if (chat.isEmpty() || r.width <= 0 || r.height <= 0) return;

        float velikost = Math.max(9f, r.height * 0.055f);
        Font font = new Font("Segoe UI", Font.PLAIN, 1).deriveFont(velikost);
        FontMetrics fm = g2.getFontMetrics(font);
        int okraj = Math.max(4, (int) (r.width * 0.04));
        int padX = Math.max(4, (int) (velikost * 0.6)), padY = Math.max(2, (int) (velikost * 0.35));
        int mezera = Math.max(3, (int) (velikost * 0.45));
        int maxSirkaTextu = (int) (r.width * 0.78) - 2 * padX;
        int radek = fm.getHeight();

        Shape puvodniClip = g2.getClip();
        g2.clip(r);
        g2.setFont(font);

        int y = r.y + r.height - okraj;
        for (int i = chat.size() - 1; i >= 0 && y > r.y; i--) {
            Zprava z = chat.get(i);
            List<String> radky = zalamText(z.text, fm, maxSirkaTextu);
            int sirkaTextu = 0;
            for (String s : radky) sirkaTextu = Math.max(sirkaTextu, fm.stringWidth(s));
            int bw = sirkaTextu + 2 * padX;
            int bh = radky.size() * radek + 2 * padY;
            int bx = z.odZakaznika ? r.x + okraj : r.x + r.width - okraj - bw;
            int by = y - bh;

            float roh = Math.min(bh, velikost * 1.6f);
            g2.setColor(z.odZakaznika ? new Color(255, 255, 255, 235) : new Color(170, 30, 36, 230));
            g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, roh, roh));
            g2.setColor(z.odZakaznika ? BARVA_TEXTU : Color.WHITE);
            int ty = by + padY + fm.getAscent();
            for (String s : radky) {
                g2.drawString(s, bx + padX, ty);
                ty += radek;
            }
            y = by - mezera;
        }
        g2.setClip(puvodniClip);
    }

    private static List<String> zalamText(String text, FontMetrics fm, int maxSirka) {
        List<String> radky = new ArrayList<>();
        StringBuilder aktualniRadek = new StringBuilder();
        for (String slovo : text.split("\\s+")) {
            String zkus = aktualniRadek.length() == 0 ? slovo : aktualniRadek + " " + slovo;
            if (fm.stringWidth(zkus) <= maxSirka || aktualniRadek.length() == 0) {
                aktualniRadek.setLength(0);
                aktualniRadek.append(zkus);
            } else {
                radky.add(aktualniRadek.toString());
                aktualniRadek.setLength(0);
                aktualniRadek.append(slovo);
            }
        }
        if (aktualniRadek.length() > 0) radky.add(aktualniRadek.toString());
        return radky;
    }

    private void kresliFotku(Graphics2D g2, Rectangle r) {
        if (aktualni < 0 || faze == Faze.PRAZDNO) return;
        BufferedImage foto = zakaznici.get(aktualni);
        float a = prechod * prechod * (3 - 2 * prechod);

        double meritko = Math.max(r.getWidth() / foto.getWidth(), r.getHeight() / foto.getHeight());
        int fw = (int) Math.ceil(foto.getWidth() * meritko);
        int fh = (int) Math.ceil(foto.getHeight() * meritko);
        int fx = r.x + (r.width - fw) / 2;
        int fy = r.y + (r.height - fh) / 3 + (int) ((1 - a) * r.height * 0.08);

        Shape puvodniClip = g2.getClip();
        float roh = r.width * 0.07f;
        g2.clip(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, roh, roh));
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, a))));
        g2.drawImage(foto, fx, fy, fw, fh, null);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setClip(puvodniClip);
    }

    private void kresliSpokojenost(Graphics2D g2, Rectangle r) {
        int okrajX = (int) (r.width * 0.06), okrajY = (int) (r.height * 0.28);
        int bx = r.x + okrajX, by = r.y + okrajY;
        int bw = r.width - 2 * okrajX, bh = r.height - 2 * okrajY;
        if (bw <= 0 || bh <= 0) return;

        g2.setColor(new Color(170, 30, 36, 40));
        g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh));

        if (faze == Faze.PRAZDNO) return;
        double podil = Math.max(0, Math.min(1, zobrazenaSpokojenost));
        int fillW = (int) Math.round(bw * podil);
        if (fillW > 0) {
            fillW = Math.max(fillW, bh);
            g2.setPaint(new GradientPaint(bx, 0, barvaSpokojenosti(podil).darker(), bx + bw, 0, barvaSpokojenosti(podil)));
            g2.fill(new RoundRectangle2D.Float(bx, by, fillW, bh, bh, bh));
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fill(new RoundRectangle2D.Float(bx, by, fillW, bh / 2f, bh / 2f, bh / 2f));
        }
    }

    private void kresliNaladu(Graphics2D g2, Rectangle r) {
        String text;
        Color barva;
        if (faze == Faze.PRAZDNO) {
            text = "Čeká se na zákazníka";
            barva = new Color(170, 30, 36, 120);
        } else {
            double s = zobrazenaSpokojenost;
            text = s >= 0.6 ? "Spokojený" : s >= 0.3 ? "Netrpělivý" : "Naštvaný";
            barva = BARVA_TEXTU;
        }

        float velikost = (float) (r.height * 0.4);
        Font font = new Font("Segoe UI", Font.BOLD, 1).deriveFont(velikost);
        while (g2.getFontMetrics(font).stringWidth(text) > r.width * 0.85 && velikost > 6f) {
            velikost -= 1f;
            font = font.deriveFont(velikost);
        }
        FontMetrics fm = g2.getFontMetrics(font);
        boolean sTeckou = faze != Faze.PRAZDNO;
        int d = (int) (fm.getAscent() * 0.55);
        int mezera = (int) (d * 0.7);
        int sirkaCelkem = fm.stringWidth(text) + (sTeckou ? d + mezera : 0);
        int x = r.x + (r.width - sirkaCelkem) / 2;
        int y = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;

        if (sTeckou) {
            g2.setColor(barvaSpokojenosti(zobrazenaSpokojenost));
            g2.fillOval(x, r.y + (r.height - d) / 2, d, d);
            x += d + mezera;
        }
        g2.setFont(font);
        g2.setColor(barva);
        g2.drawString(text, x, y);
    }

    private static Color barvaSpokojenosti(double podil) {
        Color cervena = new Color(214, 60, 60), zluta = new Color(240, 180, 40), zelena = new Color(50, 175, 90);
        if (podil < 0.5) return mix(cervena, zluta, podil / 0.5);
        return mix(zluta, zelena, (podil - 0.5) / 0.5);
    }

    private static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
