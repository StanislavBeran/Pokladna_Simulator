package cz.bazala.simulator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Grafický překryvný banner pro zprávy ze hry.
 *
 * Dva režimy zobrazení:
 *  - STRED  : zaoblený box uprostřed herní plochy (chyby, info, nová směna)
 *  - PRUH   : úzký pruh po délce obrazovky pokladny, nad ní (platba, úspěch)
 *
 * Typy:
 *   CHYBA      – červená,  STRED,  kratší doba
 *   USPECH     – zelená,   PRUH
 *   PLATBA     – zlatá,    PRUH
 *   NOVA_SMENA – modrá,    STRED
 *   INFO       – šedá,     STRED
 */
public class NotifikacniBanner extends JPanel {

    public enum Typ {
        CHYBA,
        USPECH,
        PLATBA,
        NOVA_SMENA,
        INFO
    }

    private static final int ROH          = 12;
    private static final int PRUH_VYSKA   = 44; 
    private static final int PRUH_PADDING = 10;

    private static final Font FONT_IKONA       = new Font("Segoe UI Emoji", Font.PLAIN, 20);
    private static final Font FONT_TITULEK     = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DETAIL      = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TITULEK_STR = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_DETAIL_STR  = new Font("Segoe UI", Font.PLAIN, 15);

    private String ikona   = "";
    private String titulek = "";
    private String text    = "";
    private Color  bgColor = new Color(40, 40, 40);
    private float  alfa    = 0f;
    private boolean jeStred = true;  

    private int pokladnaX = 0, pokladnaY = 0, pokladnaW = 400;

    private Timer animTimer;
    private long  faseStart;
    private enum Faze { FADE_IN, DRZI, FADE_OUT, SKRYTY }
    private Faze faze    = Faze.SKRYTY;
    private int  drzeniMs = 2000;
    private static final int FADE_IN_MS  = 150;
    private static final int FADE_OUT_MS = 250;
    private boolean trvale = false;

    private String fronterIkona, fronterTitulek, fronterText;
    private Color  fronterBarva;
    private int    fronterDrzeni;
    private boolean fronterStred;
    private boolean maFrontu = false;

    public NotifikacniBanner() {
        setOpaque(false);
        setVisible(false);
    }

    public void nastavPoziciPokladny(int x, int y, int w) {
        this.pokladnaX = x;
        this.pokladnaY = y;
        this.pokladnaW = w;
    }

    public void zobraz(Typ typ, String titulek, String detail, int drzeniMs) {
        boolean stred = jeStredTyp(typ);
        SwingUtilities.invokeLater(() -> {
            if (faze == Faze.FADE_IN || faze == Faze.DRZI) {
                fronterIkona   = ikonaProTyp(typ);
                fronterTitulek = titulek;
                fronterText    = detail != null ? detail : "";
                fronterBarva   = barvaProTyp(typ);
                fronterDrzeni  = drzeniMs;
                fronterStred   = stred;
                maFrontu = true;
                if (faze == Faze.DRZI) {
                    faze = Faze.FADE_OUT;
                    faseStart = System.currentTimeMillis();
                }
                return;
            }
            trvale = false;
            spustBanner(ikonaProTyp(typ), titulek,
                        detail != null ? detail : "",
                        barvaProTyp(typ), drzeniMs, stred);
        });
    }

    public void zobrazTrvale(Typ typ, String titulek, String detail) {
        boolean stred = jeStredTyp(typ);
        SwingUtilities.invokeLater(() -> {
            maFrontu = false;
            trvale = true;
            spustBanner(ikonaProTyp(typ), titulek,
                        detail != null ? detail : "",
                        barvaProTyp(typ), 0, stred);
        });
    }

    public void skryj() {
        SwingUtilities.invokeLater(() -> {
            if (faze == Faze.FADE_IN || faze == Faze.DRZI) {
                trvale = false;
                faze = Faze.FADE_OUT;
                faseStart = System.currentTimeMillis();
            }
        });
    }

    private void spustBanner(String ikon, String titl, String txt,
                              Color barva, int drzeni, boolean stred) {
        stopAnimTimer();
        this.ikona    = ikon;
        this.titulek  = titl;
        this.text     = txt;
        this.bgColor  = barva;
        this.drzeniMs = drzeni;
        this.jeStred  = stred;
        this.alfa     = 0f;
        this.faze     = Faze.FADE_IN;
        this.faseStart = System.currentTimeMillis();
        setVisible(true);

        animTimer = new Timer(16, e -> animKrok());
        animTimer.start();
    }

    private void animKrok() {
        long elapsed = System.currentTimeMillis() - faseStart;
        switch (faze) {
            case FADE_IN:
                alfa = Math.min(1f, (float) elapsed / FADE_IN_MS);
                if (elapsed >= FADE_IN_MS) {
                    alfa = 1f;
                    faze = Faze.DRZI;
                    faseStart = System.currentTimeMillis();
                }
                break;
            case DRZI:
                if (!trvale && elapsed >= drzeniMs) {
                    faze = Faze.FADE_OUT;
                    faseStart = System.currentTimeMillis();
                }
                break;
            case FADE_OUT:
                alfa = Math.max(0f, 1f - (float) elapsed / FADE_OUT_MS);
                if (elapsed >= FADE_OUT_MS) {
                    alfa = 0f;
                    stopAnimTimer();
                    setVisible(false);
                    faze = Faze.SKRYTY;
                    if (maFrontu) {
                        maFrontu = false;
                        spustBanner(fronterIkona, fronterTitulek, fronterText,
                                    fronterBarva, fronterDrzeni, fronterStred);
                    }
                    return;
                }
                break;
            default:
                break;
        }
        repaint();
    }

    private void stopAnimTimer() {
        if (animTimer != null) { animTimer.stop(); animTimer = null; }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alfa <= 0f) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (jeStred) {
            kresliStred(g2);
        } else {
            kresliPruh(g2);
        }
        g2.dispose();
    }

    private void kresliStred(Graphics2D g2) {
        int panW = getWidth();
        int panH = getHeight();

        boolean maDetail = !text.isEmpty();
        int boxH = 68;

        g2.setFont(FONT_IKONA);
        int ikonaW = ikona.isEmpty() ? 0 : g2.getFontMetrics().stringWidth(ikona) + 10;

        g2.setFont(FONT_TITULEK_STR);
        int titulekW = g2.getFontMetrics().stringWidth(titulek);

        int detailW = 0;
        if (maDetail) {
            g2.setFont(FONT_DETAIL_STR);
            detailW = g2.getFontMetrics().stringWidth(text) + 21; // +21 
        }

        int potrebaW = 18 + ikonaW + titulekW + detailW + 24; // levý + pravý padding
        int boxW = Math.max(300, Math.min(panW - 60, potrebaW));

        int bx = (panW - boxW) / 2;
        int by = (panH - boxH) / 2;

        kresliBox(g2, bx, by, boxW, boxH);

        int midY = by + boxH / 2;
        int curX = bx + 18;

        if (!ikona.isEmpty()) {
            g2.setFont(FONT_IKONA);
            FontMetrics fmI = g2.getFontMetrics();
            g2.setColor(new Color(255, 255, 255, (int)(alfa * 230)));
            g2.drawString(ikona, curX, midY + fmI.getAscent() / 2 - 3);
            curX += fmI.stringWidth(ikona) + 10;
        }

        g2.setFont(FONT_TITULEK_STR);
        FontMetrics fmT = g2.getFontMetrics();
        g2.setColor(new Color(0, 0, 0, (int)(alfa * 100)));
        g2.drawString(titulek, curX + 1, midY + fmT.getAscent() / 2 - 2);
        g2.setColor(new Color(255, 255, 255, (int)(alfa * 255)));
        g2.drawString(titulek, curX, midY + fmT.getAscent() / 2 - 2);

        if (maDetail) {
            curX += fmT.stringWidth(titulek) + 14;
            g2.setColor(new Color(255, 255, 255, (int)(alfa * 80)));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(curX - 7, by + 12, curX - 7, by + boxH - 12);

            g2.setFont(FONT_DETAIL_STR);
            FontMetrics fmD = g2.getFontMetrics();
            int dostupnaW = (bx + boxW) - curX - 16;
            String detailText = text;
            while (detailText.length() > 4 && fmD.stringWidth(detailText) > dostupnaW) {
                detailText = detailText.substring(0, detailText.length() - 4) + "...";
            }
            g2.setColor(new Color(220, 220, 220, (int)(alfa * 220)));
            g2.drawString(detailText, curX, midY + fmD.getAscent() / 2 - 2);
        }
    }

    private void kresliPruh(Graphics2D g2) {
        int bx = pokladnaX;
        int by = pokladnaY - PRUH_VYSKA - PRUH_PADDING;
        int bw = pokladnaW;
        int bh = PRUH_VYSKA;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa * 0.25f));
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(bx + 4, by + 5, bw, bh, ROH, ROH);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa * 0.96f));
        g2.setColor(bgColor);
        g2.fillRoundRect(bx, by, bw, bh, ROH, ROH);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa * 0.25f));
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx + 1, by + 1, bw - 2, bh - 2, ROH, ROH);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
        g2.setColor(bgColor.brighter().brighter());
        g2.fillRoundRect(bx, by, 7, bh, ROH, ROH);
        g2.fillRect(bx + 4, by, 3, bh);

        int midY = by + bh / 2;
        int curX = bx + 18;

        if (!ikona.isEmpty()) {
            g2.setFont(FONT_IKONA);
            FontMetrics fmI = g2.getFontMetrics();
            g2.setColor(new Color(255, 255, 255, (int)(alfa * 230)));
            g2.drawString(ikona, curX, midY + fmI.getAscent() / 2 - 3);
            curX += fmI.stringWidth(ikona) + 10;
        }

        g2.setFont(FONT_TITULEK);
        FontMetrics fmT = g2.getFontMetrics();
        g2.setColor(new Color(0, 0, 0, (int)(alfa * 100)));
        g2.drawString(titulek, curX + 1, midY + fmT.getAscent() / 2 - 2);
        g2.setColor(new Color(255, 255, 255, (int)(alfa * 255)));
        g2.drawString(titulek, curX, midY + fmT.getAscent() / 2 - 2);

        if (!text.isEmpty()) {
            curX += fmT.stringWidth(titulek) + 14;
            g2.setColor(new Color(255, 255, 255, (int)(alfa * 70)));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(curX - 7, by + 8, curX - 7, by + bh - 8);

            g2.setFont(FONT_DETAIL);
            FontMetrics fmD = g2.getFontMetrics();
            g2.setColor(new Color(220, 220, 220, (int)(alfa * 220)));
            String detailText = text;
            int maxW = bw - (curX - bx) - 16;
            while (fmD.stringWidth(detailText) > maxW && detailText.length() > 4) {
                detailText = detailText.substring(0, detailText.length() - 4) + "...";
            }
            g2.drawString(detailText, curX, midY + fmD.getAscent() / 2 - 2);
        }
    }

    private void kresliBox(Graphics2D g2, int bx, int by, int bw, int bh) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa * 0.30f));
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(bx + 5, by + 6, bw, bh, ROH, ROH);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa * 0.96f));
        g2.setColor(bgColor);
        g2.fillRoundRect(bx, by, bw, bh, ROH, ROH);


        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa * 0.25f));
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(bx + 1, by + 1, bw - 2, bh - 2, ROH, ROH));

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
        g2.setColor(bgColor.brighter().brighter());
        g2.fillRoundRect(bx, by, 7, bh, ROH, ROH);
        g2.fillRect(bx + 4, by, 3, bh);
    }

    private static boolean jeStredTyp(Typ typ) {
        return typ == Typ.CHYBA || typ == Typ.NOVA_SMENA || typ == Typ.INFO;
    }

    private static Color barvaProTyp(Typ typ) {
        switch (typ) {
            case CHYBA:      return new Color(185, 28, 28);
            case USPECH:     return new Color(22, 101, 52);
            case PLATBA:     return new Color(140, 95, 5);
            case NOVA_SMENA: return new Color(29, 60, 140);
            case INFO:
            default:         return new Color(45, 45, 55);
        }
    }

    private static String ikonaProTyp(Typ typ) {
        switch (typ) {
            case CHYBA:      return "⚠";
            case USPECH:     return "✓";
            case PLATBA:     return "💳";
            case NOVA_SMENA: return "🏪";
            case INFO:
            default:         return "ℹ";
        }
    }
}
