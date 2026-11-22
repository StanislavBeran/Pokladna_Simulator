package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyStroke;
import javax.swing.AbstractAction;
import java.util.ArrayList;
import java.util.List;

public class BazalaSimulator extends JPanel {
    private Menu hlavniOkno;
    private JLayeredPane vrstvy;
    private Menu.BackgroundPanel bgPanel;
    private JLabel settingsLabel;

    List<Zbozi> zboziList = new ArrayList<>();

    private PokladnaObrazovka panelObrazovky;
    private HerniPanel panelPenez;
    private HerniPanel panelXp;
    private InformacniOkno informacniOkno;
    private boolean vypinacObrazovky = true;
    private NotifikacniBanner notifikacniBanner;
    private SmenaLista smenaLista;
    private VracenePenize panelVracenychPenez;
    private HerniMenu herniMenu;
    private ObchodMenu obchodMenu;

    private JPanel fullscreenOverlay;
    private boolean isFullscreen = false;
    private final int FS_W = 1230;
    private final int FS_H = 667;

    private List<InteraktivniZona> hotspoty = new ArrayList<>();

    private final int SCREEN_X = 466;
    private final int SCREEN_Y = 95;
    private final int SCREEN_W = 420;
    private final int SCREEN_H = 218;

    private AnimovanyGif pasGif;
    private final int PAS_X = 0;
    private final int PAS_Y = 362;
    private final int PAS_W = 596;
    private final int PAS_H = 190;

    private static final String iconPath = "/nastaveni.png";
    private static final String backgroundPath = "/pokladna.png";

    private SpravcePasu spravcePasu;

    private boolean cekaNaPlatbu = false;
    private boolean platbaKartou = false;
    private int cenaNakupu = 0;
    private int castkaVratit = 0;
    private int castkaVraceno = 0;

    private final StavHry stav = new StavHry();

    private static final int XP_DELITEL_CENY = 5;      // část ceny zboží převedená na XP
    private static final int XP_ZA_KUSOVE_ZBOZI = 1;   // bonus za zboží prodávané na kusy (typ 0)
    private static final int XP_ZA_VAZENE_ZBOZI = 5;   // bonus za zboží z kategorií 1-4
    private static final int XP_BONUS_ZA_VAHU = 15;    // bonus za zboží, které se váží
    private static final int XP_ZA_UROVEN_ODEMKNUTI = 10;
    private static final int XP_FINALNI_DELITEL = 4;   // zmírnění celkového zisku XP
    private static final int XP_MINIMUM = 5;           // minimální XP za jakýkoli nákup

    private static final double PRAH_SPOKOJENOSTI_KOMBO = 0.6; // nad = kombo roste
    private static final double PRAH_NASTVANI = 0.3;           // pod = kombo se ruší

    public BazalaSimulator(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());
        vrstvy = new JLayeredPane();
        zboziList = SpravceSouboru.nactiZbozi();
        try {
            bgPanel = okno.new BackgroundPanel(backgroundPath);
            vrstvy.add(bgPanel, JLayeredPane.DEFAULT_LAYER);
            pasGif = new AnimovanyGif("/pas.gif");
            vrstvy.add(pasGif, Integer.valueOf(2));

            spravcePasu = new SpravcePasu(zboziList, pasGif, this);
            vrstvy.add(spravcePasu, Integer.valueOf(3));

            panelPenez = new HerniPanel(HerniPanel.Typ.PENIZE);
            panelPenez.setPenize(1500);
            vrstvy.add(panelPenez, Integer.valueOf(5));

            panelXp = new HerniPanel(HerniPanel.Typ.XP);

            notifikacniBanner = new NotifikacniBanner();
            vrstvy.add(notifikacniBanner, Integer.valueOf(10));

            smenaLista = new SmenaLista();
            vrstvy.add(smenaLista, Integer.valueOf(5));

            panelVracenychPenez = new VracenePenize();
            vrstvy.add(panelVracenychPenez, Integer.valueOf(4));

            herniMenu = new HerniMenu(this, hlavniOkno);
            herniMenu.setVisible(false);
            vrstvy.add(herniMenu, Integer.valueOf(8));

            obchodMenu = new ObchodMenu(this);
            obchodMenu.setVisible(false);
            vrstvy.add(obchodMenu, Integer.valueOf(9));

            informacniOkno = new InformacniOkno(() -> {
                if (spravcePasu != null) {
                    spravcePasu.odstartujPas();
                }
            });
            informacniOkno.setVisible(false);
            vrstvy.add(informacniOkno, Integer.valueOf(6));

            ImageIcon settingsIcon = new ImageIcon(getClass().getResource(iconPath));
            Image scaledImg = settingsIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            settingsLabel = new JLabel(new ImageIcon(scaledImg));
            settingsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            settingsLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (herniMenu != null) {
                        herniMenu.setVisible(true);
                    }
                }
            });
            vrstvy.add(settingsLabel, JLayeredPane.PALETTE_LAYER);

            panelObrazovky = new PokladnaObrazovka(false, zboziList, this::toggleFullscreen, this, TypZbozi.KATEGORIE_VSE, "", "", new ArrayList<>());
            vrstvy.add(panelObrazovky, Integer.valueOf(4));

            hotspoty.add(new InteraktivniZona(745, 476, 41, 72, "Platební terminál", this));
            hotspoty.add(new InteraktivniZona(800, 330, 20, 20, "vypínač", true, this));

            hotspoty.add(new InteraktivniZona(532, 580, 38, 72, "100 Kč", this));
            hotspoty.add(new InteraktivniZona(576, 580, 38, 72, "200 Kč", this));
            hotspoty.add(new InteraktivniZona(617, 580, 38, 72, "500 Kč", this));
            hotspoty.add(new InteraktivniZona(661, 580, 38, 72, "1000 Kč", this));
            hotspoty.add(new InteraktivniZona(702, 580, 38, 72, "2000 Kč", this));
            hotspoty.add(new InteraktivniZona(742, 580, 38, 72, "5000 Kč", this));

            hotspoty.add(new InteraktivniZona(533, 661, 36, 36, "1 Kč", true, this));
            hotspoty.add(new InteraktivniZona(576, 661, 36, 36, "2 Kč", true, this));
            hotspoty.add(new InteraktivniZona(617, 661, 36, 36, "5 Kč", true, this));
            hotspoty.add(new InteraktivniZona(661, 661, 36, 36, "10 Kč", true, this));
            hotspoty.add(new InteraktivniZona(703, 661, 36, 36, "20 Kč", true, this));
            hotspoty.add(new InteraktivniZona(745, 661, 36, 36, "50 Kč", true, this));

            for (InteraktivniZona zona : hotspoty) {
                vrstvy.add(zona, Integer.valueOf(1));
            }

        } catch (Exception e) {
            System.err.println("Chyba při načítání prvků: " + e.getMessage());
            setBackground(Color.DARK_GRAY);
        }

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                aktualizujRozmery();
            }
        });

        add(vrstvy, BorderLayout.CENTER);
    }

    public boolean dispatchKlavesa(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() != java.awt.event.KeyEvent.VK_SPACE) return false;
       
        if (e.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
            if (panelObrazovky == null || !panelObrazovky.isVisible()) return false;
            if (herniMenu != null && herniMenu.isVisible()) return false;
            if (obchodMenu != null && obchodMenu.isVisible()) return false;
            if (spravcePasu != null) spravcePasu.zpracujSpaceBar();
        }
        return true;
    }

    public void registrujKlavesy(javax.swing.JRootPane rootPane) {
        Object[][] mapovani = {
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, 0), '0' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, 0), '1' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, 0), '2' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, 0), '3' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, 0), '4' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, 0), '5' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_6, 0), '6' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_7, 0), '7' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_8, 0), '8' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_9, 0), '9' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD0, 0), '0' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD1, 0), '1' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD2, 0), '2' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD3, 0), '3' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD4, 0), '4' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD5, 0), '5' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD6, 0), '6' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD7, 0), '7' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD8, 0), '8' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD9, 0), '9' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MULTIPLY, 0), '*' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_8, java.awt.event.InputEvent.SHIFT_DOWN_MASK), '*' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, 0), ',' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PERIOD, 0), ',' },
            { KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DECIMAL, 0), ',' },
        };

        for (Object[] par : mapovani) {
            KeyStroke ks = (KeyStroke) par[0];
            char znak = (char) par[1];
            String akce = "gl_" + ks.toString();
            rootPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ks, akce);
            rootPane.getActionMap().put(akce, new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (panelObrazovky != null && panelObrazovky.isVisible())
                        panelObrazovky.zpracujGlobalniKlavesu(znak);
                }
            });
        }

        for (KeyStroke ks : new KeyStroke[]{
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_BACK_SPACE, 0),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0) }) {
            String akce = "gl_" + ks.toString();
            rootPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ks, akce);
            rootPane.getActionMap().put(akce, new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (panelObrazovky != null && panelObrazovky.isVisible())
                        panelObrazovky.zpracujGlobalniKlavesu('\b');
                }
            });
        }

        for (KeyStroke ks : new KeyStroke[]{
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0) }) {
            rootPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ks, "gl_enter");
            rootPane.getActionMap().put("gl_enter", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (panelObrazovky != null && panelObrazovky.isVisible())
                        panelObrazovky.zpracujGlobalniKlavesu('\n');
                }
            });
        }

        rootPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0), "gl_space");
        rootPane.getActionMap().put("gl_space", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (spravcePasu != null) spravcePasu.zpracujSpaceBar();
            }
        });
    }

    private void aktualizujRozmery() {
        int w = getWidth();
        int h = getHeight();
        vrstvy.setBounds(0, 0, w, h);

        if (bgPanel != null) bgPanel.setBounds(0, 0, w, h);
        if (settingsLabel != null) {
            settingsLabel.setBounds(w - 80, h - 80, 60, 60);
        }
        if (pasGif != null) {
            double scaleW = Rozmery.scaleX(getWidth());
            double scaleH = Rozmery.scaleY(getHeight());
            int scaledX = (int)(PAS_X * scaleW);
            int scaledY = (int)(PAS_Y * scaleH);
            int scaledW = (int)(PAS_W * scaleW);
            int scaledH = (int)(PAS_H * scaleH);
            pasGif.setBounds(scaledX, scaledY, scaledW, scaledH);
            if (spravcePasu != null) {
                spravcePasu.setBounds(scaledX, scaledY, (int)(Rozmery.REF_SIRKA * scaleW), scaledH);
            }

        }

        double scaleW = Rozmery.scaleX(w);
        double scaleH = Rozmery.scaleY(h);

        if (isFullscreen && fullscreenOverlay != null) {
            int fw = (int)(FS_W * scaleW);
            int fh = (int)(FS_H * scaleH);
            fullscreenOverlay.setBounds((w - fw) / 2, (h - fh) / 2, fw, fh);
            panelObrazovky.setBounds(0, 0, fw, fh);
        } else if (panelObrazovky != null) {
            panelObrazovky.setBounds((int)(SCREEN_X * scaleW), (int)(SCREEN_Y * scaleH),
                    (int)(SCREEN_W * scaleW), (int)(SCREEN_H * scaleH));
        }
        if (panelPenez != null) {
            int sirkaBaru = (int)(280 * scaleW);
            int vyskaBaru = (int)(60 * scaleH);
            panelPenez.setBounds((int)(20 * scaleW), (int)(15 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (panelXp != null) {
            int sirkaBaru = (int)(280 * scaleW);
            int vyskaBaru = (int)(60 * scaleH);
            panelXp.setBounds(w - sirkaBaru - (int)(20 * scaleW), (int)(15 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (smenaLista != null) {
            int sirka = (int)(580 * scaleW);
            int vyska = (int)(22 * scaleH);
            int x = (w - sirka) / 2;
            int y = (int)(15 * scaleH);
            smenaLista.setBounds(x, y, sirka, vyska);
        }
        if (notifikacniBanner != null) {
            notifikacniBanner.setBounds(0, 0, w, h);
            if (panelObrazovky != null) {
                int sx = (int)(SCREEN_X * scaleW);
                int sy = (int)(SCREEN_Y * scaleH);
                int sw = (int)(SCREEN_W * scaleW);
                notifikacniBanner.nastavPoziciPokladny(sx, sy, sw);
            }
        }
        if (panelVracenychPenez != null) {
            panelVracenychPenez.setBounds((int)(1120 * scaleW), (int)(320 * scaleH), (int)(150 * scaleW), (int)(150 * scaleH));
        }
        if (herniMenu != null) {
            herniMenu.setBounds(0, 0, w, h);
        }
        if (obchodMenu != null) {
            obchodMenu.setBounds(0, 0, w, h);
        }
        for (InteraktivniZona zona : hotspoty) {
            zona.aktualizujPozici(scaleW, scaleH);
        }
        if (informacniOkno != null) {
            informacniOkno.setBounds(0, 0, w, h);
        }
        revalidate();
        repaint();
    }
    public void pridejZboziNaUctenku(Zbozi z, int mnozstvi) {
        if (panelObrazovky != null) {
            panelObrazovky.pridejPolozkuNaUctenku(z, mnozstvi);
        }
    }
    public boolean overZboziZeScanneru(int id, int mnozstvi) {
        if (spravcePasu != null) {
            return spravcePasu.overAOdjedZeScanneru(id, mnozstvi);
        }
        return false;
    }
    private void toggleFullscreen() {
        int ulozenaKategorie = panelObrazovky.getAktualniKategorie();
        String ulozeneHledani = panelObrazovky.getHledanyText();
        String ulozenyNumpad = panelObrazovky.getNumpadText();
        List<Zbozi> ulozenaUctenka = panelObrazovky.getPolozkyNaUctence();

        isFullscreen = !isFullscreen;

        if (isFullscreen) {
            vrstvy.remove(panelObrazovky);
            panelObrazovky = new PokladnaObrazovka(true, zboziList, this::toggleFullscreen, this, ulozenaKategorie, ulozeneHledani, ulozenyNumpad, ulozenaUctenka);
            if (fullscreenOverlay == null) {
                fullscreenOverlay = new JPanel(new BorderLayout());
                fullscreenOverlay.setOpaque(false);
            }
            fullscreenOverlay.removeAll();
            fullscreenOverlay.add(panelObrazovky, BorderLayout.CENTER);

            vrstvy.add(fullscreenOverlay, JLayeredPane.DRAG_LAYER);
            fullscreenOverlay.setVisible(true);
        } else {
            if (fullscreenOverlay != null) {
                fullscreenOverlay.remove(panelObrazovky);
                vrstvy.remove(fullscreenOverlay);
                fullscreenOverlay.setVisible(false);
            }
            panelObrazovky = new PokladnaObrazovka(false, zboziList, this::toggleFullscreen, this,
                    ulozenaKategorie, ulozeneHledani, ulozenyNumpad, ulozenaUctenka);
            vrstvy.add(panelObrazovky, Integer.valueOf(3));
        }

        panelObrazovky.setVisible(vypinacObrazovky);
        aktualizujRozmery();
    }

    public void prepniVypinac() {
        vypinacObrazovky = !vypinacObrazovky;
        if (panelObrazovky != null) {
            panelObrazovky.setVisible(vypinacObrazovky);
        }
    }
    public void spustSimulaci() {
        if (spravcePasu != null) {
            spravcePasu.odstartujPas();
        }
    }
    public void nactiPenize(int penize) {
        stav.setPenize(penize);
        if (panelPenez != null) {
            panelPenez.setPenize(penize);
        }
    }
    public String getJmenoObchodu(){
        return stav.getJmenoObchodu();
    }
    public void nactiXp(int celkoveXp) {
        stav.setCelkoveXp(celkoveXp);

        if (panelXp != null) {
            panelXp.setXpData(stav.getLevel(), stav.getXpDoDalsihoLevelu(), stav.getMaxXpNaLevel());
        }
    }
    public void nactiVylepseni(String vylepseni) {
        stav.setVylepseni(vylepseni);
        aplikujVsechnaVylepseni();
    }
    public void nactiOdemceneZbozi(List<Integer> odemceneZbozi) {
        stav.setOdemceneZbozi(odemceneZbozi);
        if (panelObrazovky != null) {
            panelObrazovky.obnovSeznamZbozi();
        }
        if (obchodMenu != null) {
            obchodMenu.aktualizujNabidku();
        }
    }
    public void zobrazNavodANastartujHru() {
        if (informacniOkno != null) {
            informacniOkno.setVisible(true);
        }
    }
    public List<Integer> getOdemceneZbozi(){
        return stav.getOdemceneZbozi();
    }
    public void zobrazVahu(int vaha) {
        if (panelObrazovky != null) {
            panelObrazovky.nastavVahu(vaha);
        }
    }
    public void vypisDoKonzole(String text) {
        if (text.contains("Chyba")) {
            SpravceZvuku.prehraj("chyba");
            String detail = text.startsWith("Chyba: ") ? text.substring(7) : text;
            if (notifikacniBanner != null) {
                notifikacniBanner.zobraz(NotifikacniBanner.Typ.CHYBA, "Chyba", detail, 1400);
            }
        } else {
            if (notifikacniBanner != null) {
                notifikacniBanner.zobraz(NotifikacniBanner.Typ.INFO, text, "", 2500);
            }
        }
    }
    public boolean cekaNaPlatbu() {
        return cekaNaPlatbu;
    }
    public void zahajPlatbu() {
        cenaNakupu = panelObrazovky.getCelkovaCena();
        if (cenaNakupu == 0) {
            if (notifikacniBanner != null) {
                notifikacniBanner.zobraz(NotifikacniBanner.Typ.INFO, "Prázdný nákup", "Zákazník nic nekoupil.", 2200);
            }
            dokonciPlatbu(false);
            return;
        }

        cekaNaPlatbu = true;
        platbaKartou = Math.random() < 0.5;
        if (platbaKartou) {
            if (notifikacniBanner != null) {
                notifikacniBanner.zobrazTrvale(NotifikacniBanner.Typ.PLATBA,
                        "Platba kartou",
                        "Zákazník platí " + cenaNakupu + " Kč  •  Klikni na Platební terminál");
            }
            panelObrazovky.nastavStavPlatby(false, 0, 0);
        } else {
            int[] bankovky = {100, 200, 500, 1000, 2000, 5000};
            int dalZakanik = 0;
            for (int b : bankovky) {
                if (b > cenaNakupu) {
                    dalZakanik = b;
                    break;
                }
            }
            if (dalZakanik == 0) dalZakanik = cenaNakupu + 1000;

            castkaVratit = dalZakanik - cenaNakupu;
            castkaVraceno = 0;

            if (notifikacniBanner != null) {
                notifikacniBanner.zobrazTrvale(NotifikacniBanner.Typ.PLATBA,
                        "Platba hotově",
                        "Dal ti " + dalZakanik + " Kč  •  Vrátit: " + castkaVratit + " Kč");
            }
            panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
        }
    }

    public void zpracujKliknutiNaPolozku(String nazev) {
        if (!cekaNaPlatbu) return;

        if (platbaKartou) {
            if (nazev.equals("Platební terminál")) {
                if (notifikacniBanner != null) notifikacniBanner.skryj();
                if (notifikacniBanner != null) {
                    notifikacniBanner.zobraz(NotifikacniBanner.Typ.USPECH, "Platba kartou přijata!", "", 2000);
                }
                dokonciPlatbu(true);
            }
        } else {
            if (nazev.equals("Platební terminál")) {
                if (notifikacniBanner != null) {
                    notifikacniBanner.zobraz(NotifikacniBanner.Typ.INFO, "Zákazník platí hotově", "Vrať správné mince z kasy.", 2200);
                }
                return;
            }
            try {
                int hodnota = Integer.parseInt(nazev.replace(" Kč", "").trim());
                castkaVraceno += hodnota;
                panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
                if (panelVracenychPenez != null) {
                    panelVracenychPenez.pridejPenize(hodnota);
                }
                if (castkaVraceno == castkaVratit) {
                    if (notifikacniBanner != null) notifikacniBanner.skryj();
                    dokonciPlatbu(true);
                } else if (castkaVraceno > castkaVratit) {
                    castkaVraceno = 0;
                    panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
                    if (panelVracenychPenez != null) {
                        panelVracenychPenez.vycistiHromadku();
                    }
                    SpravceZvuku.prehraj("chyba");
                    if (notifikacniBanner != null) {
                        notifikacniBanner.zobraz(NotifikacniBanner.Typ.CHYBA, "Příliš mnoho!", "Vracíš víc než máš. Reset na 0.", 1400);
                    }
                }
            } catch (NumberFormatException e) {
                if (notifikacniBanner != null) {
                    notifikacniBanner.zobraz(NotifikacniBanner.Typ.CHYBA, "Chyba", "Neznámá hodnota platidla.", 1100);
                }
            }
        }
    }

    private void dokonciPlatbu(boolean uspesne) {
        cekaNaPlatbu = false;
        if (panelVracenychPenez != null) {
            panelVracenychPenez.vycistiHromadku();
        }
        if (uspesne) {
            SpravceZvuku.prehraj("mince");
            double spokojenost = (spravcePasu != null) ? spravcePasu.getPomerTrpelivosti() : 1.0;

            if (spokojenost >= PRAH_SPOKOJENOSTI_KOMBO) {
                stav.zvysKombo();
            } else if (spokojenost < PRAH_NASTVANI) {
                stav.vynulujKombo();
            }

            double faktorSpokojenosti = 0.5 + 0.5 * spokojenost; 
            double nasobic = stav.getNasobicOdmen() * stav.getNasobicKomba() * faktorSpokojenosti;

            int zakladXp = vypocitejXpZaNakup(panelObrazovky.getPolozkyNaUctence());
            int xpZaNakup = Math.max(1, (int) Math.round(zakladXp * nasobic));
            int penizeZaNakup = Math.max(0, (int) Math.round(cenaNakupu * nasobic));

            nactiXp(stav.getCelkoveXp() + xpZaNakup);
            nactiPenize(stav.getPenize() + penizeZaNakup);

            boolean cilSplnen = stav.zaevidujNakup(penizeZaNakup);

            if (smenaLista != null) {
                smenaLista.aktualizuj(stav.getCisloSmeny(), stav.getTrzbaSmeny(), stav.getCilSmeny());
            }

            StringBuilder zprava = new StringBuilder();
            zprava.append("Získáno ").append(penizeZaNakup).append(" Kč a ").append(xpZaNakup).append(" XP");
            if (stav.getKomboSerie() >= 2) {
                zprava.append(" (kombo x").append(stav.getKomboSerie()).append(")");
            }
            zprava.append(".");
            if (notifikacniBanner != null) {
                notifikacniBanner.zobraz(NotifikacniBanner.Typ.USPECH, "Nákup dokončen", zprava.toString(), 2500);
            }

            if (spokojenost < PRAH_NASTVANI) {
                if (notifikacniBanner != null) {
                    notifikacniBanner.zobraz(NotifikacniBanner.Typ.CHYBA, "Zákazník odešel naštvaný", "Kombo ztraceno.", 1250);
                }
            }

            if (cilSplnen) {
                oznamSplneniCile();
            }
        }
        panelObrazovky.vycistiUctenku();

        if (spravcePasu != null) {
            spravcePasu.dokonciNakupAZacniNovy();
        }
    }


    private void oznamSplneniCile() {        int odmena = 300 + stav.getCisloSmeny() * 100;
        nactiPenize(stav.getPenize() + odmena);
        stav.setCisloSmeny(stav.getCisloSmeny() + 1);
        stav.zahajNovouSmenu();
        if (smenaLista != null) {
            smenaLista.aktualizuj(stav.getCisloSmeny(), stav.getTrzbaSmeny(), stav.getCilSmeny());
        }
        if (notifikacniBanner != null) {
            notifikacniBanner.zobraz(NotifikacniBanner.Typ.NOVA_SMENA,
                    "Směna " + (stav.getCisloSmeny() - 1) + " splněna!",
                    "Bonus " + odmena + " Kč  •  Začíná směna " + stav.getCisloSmeny(), 4500);
        }
    }

    private int vypocitejXpZaNakup(List<Zbozi> uctenka) {
        int ziskaneXp = 0;

        for (Zbozi z : uctenka) {
            int xpZaPolozku = z.cena / XP_DELITEL_CENY;
            if (z.jeKusove()) {
                xpZaPolozku += XP_ZA_KUSOVE_ZBOZI;
            } else if (z.jeSkenovane()) {
                xpZaPolozku += XP_ZA_VAZENE_ZBOZI;
            }
            if (z.minVaha > 0 && z.maxVaha > 0) {
                xpZaPolozku += XP_BONUS_ZA_VAHU;
            }
            xpZaPolozku += z.lvlOdemknuti * XP_ZA_UROVEN_ODEMKNUTI;
            if (z.minVaha == 0 && z.maxVaha == 0) {
                ziskaneXp += xpZaPolozku * Math.max(1, z.maxPocet);
            } else {
                ziskaneXp += xpZaPolozku;
            }
        }

        return Math.max(XP_MINIMUM, ziskaneXp / XP_FINALNI_DELITEL);
    }
    public String getVybranaHudba() {
        return stav.getVybranaHudba();
    }
    public List<String> getOdemcenaHudba() {
        return stav.getOdemcenaHudba();
    }
    public void nastavVybranouHudbu(String hudba) {
        stav.setVybranaHudba(hudba);
    }
    public void nastavDetailyHry(int slot, String jmeno, String vybrana, List<String> odemcena) {
        stav.setAktualniSlot(slot);
        stav.setJmenoObchodu(jmeno);
        stav.setHraNactena(true);
        if (panelObrazovky != null) {
            panelObrazovky.vycistiUctenku();
        }
        stav.setVybranaHudba(vybrana);
        stav.setOdemcenaHudba(odemcena);

        stav.zahajNovouSmenu();
        stav.vynulujKombo();
        if (smenaLista != null) {
            smenaLista.aktualizuj(stav.getCisloSmeny(), stav.getTrzbaSmeny(), stav.getCilSmeny());
        }
    }


    public void nactiStatistiky(int celkemObslouzeno, int nejlepsiKombo, int nejlepsiTrzbaSmeny,
                                int celkemVydelano, int cisloSmeny) {
        stav.setCelkemObslouzeno(celkemObslouzeno);
        stav.setNejlepsiKombo(nejlepsiKombo);
        stav.setNejlepsiTrzbaSmeny(nejlepsiTrzbaSmeny);
        stav.setCelkemVydelano(celkemVydelano);
        stav.setCisloSmeny(cisloSmeny);
    }
    public int getAktualniPenize() {
        return stav.getPenize();
    }

    public int getCisloSmeny() {
        return stav.getCisloSmeny();
    }

    public void odectiPenize(int castka) {
        nactiPenize(stav.getPenize() - castka);
    }

    public int getLevel() {
        return stav.getLevel();
    }

    public List<Zbozi> getZboziList() {
        return zboziList;
    }

    public void odemkniZbozi(int id) {
        if (stav.odemkniZbozi(id)) {
            if (panelObrazovky != null) {
                panelObrazovky.obnovSeznamZbozi();
            }
        }
    }
    public void ulozHru() {
        if (!stav.jeHraNactena()) return;
        SpravceSouboru.ulozHruDoSouboru(stav.getAktualniSlot(), stav.getJmenoObchodu(), String.valueOf(stav.getPenize()),
                stav.getCelkoveXp(), stav.getVylepseni(), stav.getOdemceneZbozi(), stav.getVybranaHudba(), stav.getOdemcenaHudba(),
                stav.getCelkemObslouzeno(), stav.getNejlepsiKombo(), stav.getNejlepsiTrzbaSmeny(),
                stav.getCelkemVydelano(), stav.getCisloSmeny());
        System.out.println("Hra byla úspěšně uložena!");
    }
    public void zobrazObchod() {
        if (obchodMenu != null) {
            obchodMenu.aktualizujNabidku();
            obchodMenu.setVisible(true);
        }
    }

    public void pozastavHru() {
        if (spravcePasu != null) spravcePasu.pozastav();
    }

    public void obnovHru() {
        if (spravcePasu != null && stav.jeHraNactena()) spravcePasu.obnov();
    }
    public int getUrovenRychlostiPasu() {
        return stav.getUrovenRychlostiPasu();
    }

    public void zvysUrovenRychlostiPasu() {
        int novaUroven = stav.zvysUrovenRychlostiPasu();
        if (novaUroven >= 0 && spravcePasu != null) {
            spravcePasu.aplikujVylepseniRychlosti(novaUroven);
        }
    }

    public double getNasobicTrpelivosti() {
        return stav.getNasobicTrpelivosti();
    }

    public boolean maDruhyScanner() {
        return stav.maDruhyScanner();
    }

    public boolean maAutoSkenKusove() {
        return stav.maAutoSkenKusove();
    }

    public int getUrovenVylepseni(StavHry.Vylepseni v) {
        return stav.getUrovenVylepseni(v);
    }

    public boolean kupVylepseni(StavHry.Vylepseni druh, int cena) {
        if (stav.getPenize() < cena) {
            vypisDoKonzole("Chyba: Nedostatek peněz na vylepšení!");
            return false;
        }
        int novaUroven = stav.zvysUrovenVylepseni(druh);
        if (novaUroven < 0) {
            vypisDoKonzole("Chyba: Toto vylepšení je už na maximu!");
            return false;
        }
        odectiPenize(cena);
        aplikujVsechnaVylepseni();
        return true;
    }

    private void aplikujVsechnaVylepseni() {
        if (spravcePasu != null) {
            spravcePasu.aplikujVylepseniRychlosti(stav.getUrovenRychlostiPasu());
        }
    }
    public void resetujHerniPole() {
        stav.setHraNactena(false);
        cekaNaPlatbu = false;
        platbaKartou = false;
        if (spravcePasu != null) {
            spravcePasu.vycistiVsechno();
        }
        if (panelObrazovky != null) {
            panelObrazovky.vycistiUctenku();
            panelObrazovky.nastavVahu(0);
            panelObrazovky.nastavStavPlatby(false, 0, 0);
        }
        if (panelVracenychPenez != null) {
            panelVracenychPenez.vycistiHromadku();
        }

    }

    public void resetujZakaznika() {
        cekaNaPlatbu = false;
        platbaKartou = false;
        if (spravcePasu != null) {
            spravcePasu.vycistiVsechno();
        }
        if (panelObrazovky != null) {
            panelObrazovky.vycistiUctenku();
            panelObrazovky.nastavVahu(0);
            panelObrazovky.nastavStavPlatby(false, 0, 0);
        }
        if (panelVracenychPenez != null) {
            panelVracenychPenez.vycistiHromadku();
        }
        stav.vynulujKombo();
        spustSimulaci();
    }
    public boolean kupHudbu(String souborHudby, int cena) {
        StavHry.KoupeneHudby vysledek = stav.zkusKoupitHudbu(souborHudby, cena);
        switch (vysledek) {
            case KOUPENO:
                nactiPenize(stav.getPenize());
                vypisDoKonzole("Zakoupena nová hudba! Můžeš ji změnit v Nastavení.");
                return true;
            case VLASTNI:
                vypisDoKonzole("Chyba: Tuto hudbu už vlastníš!");
                return false;
            case NEDOSTATEK_PENEZ:
            default:
                vypisDoKonzole("Chyba: Nedostatek peněz!");
                return false;
        }
    }
}