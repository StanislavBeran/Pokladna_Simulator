package cz.pokladna.simulator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class Vzhled {

    static final Color POZADI = new Color(22, 25, 32);
    static final Color PANEL = new Color(34, 38, 48);
    static final Color PANEL_SVETLY = new Color(48, 54, 68);
    static final Color PANEL_HOVER = new Color(62, 70, 88);
    static final Color AKCENT = new Color(58, 122, 232);
    static final Color AKCENT_HOVER = new Color(84, 144, 248);
    static final Color AKCENT_STISK = new Color(44, 98, 196);
    static final Color ZELENA = new Color(38, 166, 92);
    static final Color CERVENA = new Color(214, 76, 76);
    static final Color ZLATA = new Color(255, 200, 40);
    static final Color TEXT = new Color(232, 234, 240);
    static final Color TEXT_TLUMENY = new Color(146, 152, 168);

    static final String IKONA = "vzhled.ikona";
    static final String VYPNUTA_BARVA = "vzhled.vypnutaBarva";
    static final String VYPNUTY_TEXT = "vzhled.vypnutyText";
    static final String BARVA = "vzhled.barva";

    private Vzhled() {
    }

    static Graphics2D vyhlazene(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
    }

    static String zkrat(String text, FontMetrics fm, int maxSirka) {
        if (text == null) return "";
        if (fm.stringWidth(text) <= maxSirka) return text;
        int konec = text.length();
        while (konec > 0 && fm.stringWidth(text.substring(0, konec) + "…") > maxSirka) konec--;
        return konec <= 0 ? "" : text.substring(0, konec).trim() + "…";
    }

    static List<String> zalom(String text, FontMetrics fm, int maxSirka) {
        List<String> radky = new ArrayList<>();
        if (text == null || text.isEmpty()) return radky;
        StringBuilder radek = new StringBuilder();
        for (String slovo : text.split(" ")) {
            String kandidat = radek.length() == 0 ? slovo : radek + " " + slovo;
            if (fm.stringWidth(kandidat) > maxSirka && radek.length() > 0) {
                radky.add(radek.toString());
                radek = new StringBuilder(slovo);
            } else {
                radek = new StringBuilder(kandidat);
            }
        }
        if (radek.length() > 0) radky.add(radek.toString());
        return radky;
    }

    static String kc(int castka) {
        String cislo = Integer.toString(Math.abs(castka));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cislo.length(); i++) {
            if (i > 0 && (cislo.length() - i) % 3 == 0) sb.append('\u00A0');
            sb.append(cislo.charAt(i));
        }
        return (castka < 0 ? "-" : "") + sb + "\u00A0Kč";
    }

    static void kresliMinci(Graphics2D g2, double x, double y, double d) {
        g2.setPaint(new RadialGradientPaint(
                new Point.Double(x + d * 0.35, y + d * 0.3), (float) (d * 0.7),
                new float[]{0f, 1f}, new Color[]{new Color(255, 232, 130), new Color(214, 150, 20)}));
        g2.fill(new Ellipse2D.Double(x, y, d, d));
        g2.setColor(new Color(150, 98, 10));
        g2.setStroke(new BasicStroke((float) Math.max(1, d * 0.07)));
        g2.draw(new Ellipse2D.Double(x + d * 0.18, y + d * 0.18, d * 0.64, d * 0.64));
    }

    static void kresliStin(Graphics2D g2, int x, int y, int w, int h, int roh) {
        for (int i = 0; i < 8; i++) {
            g2.setColor(new Color(0, 0, 0, 14));
            g2.fill(new RoundRectangle2D.Float(x - i, y - i + 4, w + 2 * i, h + 2 * i, roh + i, roh + i));
        }
    }

    static JButton tlacitko(String text, Color zaklad, Color hover, Color stisk, Font font, Color barvaTextu) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = vyhlazene(g);
                int w = getWidth(), h = getHeight();
                int roh = Math.min(h, 14);

                Color pozadi;
                Color popredi = barvaTextu;
                if (!isEnabled()) {
                    Object vb = getClientProperty(VYPNUTA_BARVA);
                    Object vt = getClientProperty(VYPNUTY_TEXT);
                    pozadi = vb instanceof Color ? (Color) vb : PANEL_SVETLY;
                    popredi = vt instanceof Color ? (Color) vt : TEXT_TLUMENY;
                } else if (getClientProperty(BARVA) instanceof Color) {
                    Color vlastni = (Color) getClientProperty(BARVA);
                    pozadi = getModel().isPressed() ? vlastni.darker() : getModel().isRollover() ? vlastni.brighter() : vlastni;
                    popredi = Color.WHITE;
                } else if (getModel().isPressed()) {
                    pozadi = stisk;
                } else if (getModel().isRollover()) {
                    pozadi = hover;
                } else {
                    pozadi = zaklad;
                }
                g2.setColor(pozadi);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, roh, roh));

                FontMetrics fm = g2.getFontMetrics(getFont());
                Object ikonaObj = getClientProperty(IKONA);
                String ikona = ikonaObj instanceof String ? (String) ikonaObj : null;
                Font fontIkony = new Font("Segoe UI Symbol", Font.PLAIN, getFont().getSize());
                FontMetrics fmI = g2.getFontMetrics(fontIkony);
                int mezera = ikona != null ? fmI.stringWidth(ikona) + fm.stringWidth(" ") : 0;
                String t = zkrat(getText(), fm, w - 12 - mezera);
                int sirka = fm.stringWidth(t) + mezera;
                int x = (w - sirka) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2;

                g2.setColor(popredi);
                if (ikona != null) {
                    g2.setFont(fontIkony);
                    g2.drawString(ikona, x, y);
                    x += mezera;
                }
                g2.setFont(getFont());
                g2.drawString(t, x, y);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setOpaque(false);
        btn.setRolloverEnabled(true);
        btn.setFont(font);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JButton primarni(String text, Font font) {
        return tlacitko(text, AKCENT, AKCENT_HOVER, AKCENT_STISK, font, Color.WHITE);
    }

    static JButton sekundarni(String text, Font font) {
        return tlacitko(text, PANEL_SVETLY, PANEL_HOVER, PANEL, font, TEXT);
    }

    static void sledujHover(JComponent koren, Consumer<Boolean> naZmenu) {
        MouseAdapter posluchac = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { naZmenu.accept(true); }
            @Override public void mouseExited(MouseEvent e) { naZmenu.accept(koren.getMousePosition(true) != null); }
        };
        pridejRekurzivne(koren, posluchac);
    }

    private static void pridejRekurzivne(Component c, MouseAdapter posluchac) {
        c.addMouseListener(posluchac);
        if (c instanceof Container) {
            for (Component dite : ((Container) c).getComponents()) pridejRekurzivne(dite, posluchac);
        }
    }

    static class Karta extends JPanel {
        private final Color barva;
        private final int roh;
        private Color ramecek;
        private boolean zvyraznena;

        Karta(Color barva, int roh) {
            this.barva = barva;
            this.roh = roh;
            setOpaque(false);
        }

        void setRamecek(Color ramecek) {
            this.ramecek = ramecek;
            repaint();
        }

        void setZvyraznena(boolean zvyraznena) {
            if (this.zvyraznena != zvyraznena) {
                this.zvyraznena = zvyraznena;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = vyhlazene(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(zvyraznena ? PANEL_HOVER : barva);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, roh, roh));
            Color r = zvyraznena ? AKCENT : ramecek;
            if (r != null) {
                g2.setColor(r);
                g2.setStroke(new BasicStroke(zvyraznena ? 2f : 1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, roh, roh));
            }
            g2.dispose();
        }
    }

    static class Seznam extends JPanel implements Scrollable {
        Seznam(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(1, r.height - 24); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    static JScrollPane scroll(JComponent obsah) {
        JScrollPane scroll = new JScrollPane(obsah);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        JScrollBar lista = scroll.getVerticalScrollBar();
        lista.setUI(new TenkyScrollBarUI(false));
        lista.setOpaque(false);
        lista.setUnitIncrement(24);
        lista.setPreferredSize(new Dimension(8, 0));
        return scroll;
    }
}
