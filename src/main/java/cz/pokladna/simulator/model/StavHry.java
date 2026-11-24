package cz.pokladna.simulator.model;

import java.util.ArrayList;
import java.util.List;


public class StavHry {

    
    public enum KoupeneHudby {
        KOUPENO,
        VLASTNI,
        NEDOSTATEK_PENEZ
    }
    public enum Vylepseni {
        RYCHLOST_PASU(0, 4),      
        BONUS_ODMEN(1, 4),        
        TRPELIVOST(2, 4), 
        DRUHY_SCANNER(3, 1),      
        AUTO_SKEN_KUSOVE(4, 1);   

        public final int pozice;
        public final int maxUroven;

        Vylepseni(int pozice, int maxUroven) {
            this.pozice = pozice;
            this.maxUroven = maxUroven;
        }
    }

    public static final int POCET_VYLEPSENI = 5;
    private static final int XP_NA_LEVEL = 100;
    private static final int VYCHOZI_ODEMCENE_ID = 16;
    private static final String VYCHOZI_VYLEPSENI = "00000";

    private int penize = 0;
    private int celkoveXp = 0;
    private String vylepseni = VYCHOZI_VYLEPSENI;
    private List<Integer> odemceneZbozi = new ArrayList<>(List.of(VYCHOZI_ODEMCENE_ID));
    private String jmenoObchodu = "Můj Obchod";
    private int aktualniSlot = 1;
    private String vybranaHudba = "obchod_theme";
    private List<String> odemcenaHudba = new ArrayList<>(List.of("obchod_theme"));
    private boolean hraJeNactena = false;

    private int komboSerie = 0;

    private int celkemObslouzeno = 0;
    private int nejlepsiKombo = 0;
    private int nejlepsiTrzbaSmeny = 0;
    private int celkemVydelano = 0;

    private int cilSmeny = 0;
    private int trzbaSmeny = 0;
    private int cisloSmeny = 1;

    public int getPenize() {
        return penize;
    }

    public void setPenize(int penize) {
        this.penize = penize;
    }

    public void pridejPenize(int castka) {
        this.penize += castka;
    }

    public void odectiPenize(int castka) {
        this.penize -= castka;
    }

    public int getCelkoveXp() {
        return celkoveXp;
    }

    public void setCelkoveXp(int celkoveXp) {
        this.celkoveXp = celkoveXp;
    }

    public int getLevel() {
        return (celkoveXp / XP_NA_LEVEL) + 1;
    }

    public int getXpDoDalsihoLevelu() {
        return celkoveXp % XP_NA_LEVEL;
    }

    public int getMaxXpNaLevel() {
        return XP_NA_LEVEL;
    }

    public String getVylepseni() {
        return vylepseni;
    }

    public void setVylepseni(String vylepseni) {
        this.vylepseni = normalizujVylepseni(vylepseni);
    }

    /** Zajistí, že řetězec vylepšení má správnou délku a jen číslice. */
    private static String normalizujVylepseni(String vstup) {
        StringBuilder sb = new StringBuilder(VYCHOZI_VYLEPSENI);
        if (vstup != null) {
            for (int i = 0; i < POCET_VYLEPSENI && i < vstup.length(); i++) {
                char c = vstup.charAt(i);
                if (Character.isDigit(c)) sb.setCharAt(i, c);
            }
        }
        return sb.toString();
    }

    public int getUrovenVylepseni(Vylepseni v) {
        if (vylepseni == null || v.pozice >= vylepseni.length()) return 0;
        int uroven = Character.getNumericValue(vylepseni.charAt(v.pozice));
        return (uroven < 0) ? 0 : Math.min(uroven, v.maxUroven);
    }

    public int zvysUrovenVylepseni(Vylepseni v) {
        int current = getUrovenVylepseni(v);
        if (current >= v.maxUroven) return -1;
        current++;
        StringBuilder sb = new StringBuilder(normalizujVylepseni(vylepseni));
        sb.setCharAt(v.pozice, Character.forDigit(current, 10));
        vylepseni = sb.toString();
        return current;
    }


    public int getUrovenRychlostiPasu() {
        return getUrovenVylepseni(Vylepseni.RYCHLOST_PASU);
    }

    public int zvysUrovenRychlostiPasu() {
        return zvysUrovenVylepseni(Vylepseni.RYCHLOST_PASU);
    }

    public double getNasobicOdmen() {
        return 1.0 + 0.15 * getUrovenVylepseni(Vylepseni.BONUS_ODMEN);
    }

    public double getNasobicTrpelivosti() {
        return 1.0 + 0.3 * getUrovenVylepseni(Vylepseni.TRPELIVOST);
    }

    public boolean maDruhyScanner() {
        return getUrovenVylepseni(Vylepseni.DRUHY_SCANNER) >= 1;
    }

    public boolean maAutoSkenKusove() {
        return getUrovenVylepseni(Vylepseni.AUTO_SKEN_KUSOVE) >= 1;
    }

    public List<Integer> getOdemceneZbozi() {
        return odemceneZbozi;
    }

    public void setOdemceneZbozi(List<Integer> odemceneZbozi) {
        if (odemceneZbozi == null) {
            this.odemceneZbozi = new ArrayList<>(List.of(VYCHOZI_ODEMCENE_ID));
        } else {
            this.odemceneZbozi = odemceneZbozi;
        }
    }

    public boolean odemkniZbozi(int id) {
        if (odemceneZbozi.contains(id)) return false;
        odemceneZbozi.add(id);
        return true;
    }

    public String getJmenoObchodu() {
        return jmenoObchodu;
    }

    public void setJmenoObchodu(String jmenoObchodu) {
        this.jmenoObchodu = jmenoObchodu;
    }

    public int getAktualniSlot() {
        return aktualniSlot;
    }

    public void setAktualniSlot(int aktualniSlot) {
        this.aktualniSlot = aktualniSlot;
    }

    public boolean jeHraNactena() {
        return hraJeNactena;
    }

    public void setHraNactena(boolean hraJeNactena) {
        this.hraJeNactena = hraJeNactena;
    }

    public String getVybranaHudba() {
        return vybranaHudba;
    }

    public void setVybranaHudba(String vybranaHudba) {
        this.vybranaHudba = (vybranaHudba != null && !vybranaHudba.isEmpty()) ? vybranaHudba : "obchod_theme";
    }

    public List<String> getOdemcenaHudba() {
        return odemcenaHudba;
    }

    public void setOdemcenaHudba(List<String> odemcenaHudba) {
        if (odemcenaHudba != null && !odemcenaHudba.isEmpty()) {
            this.odemcenaHudba = odemcenaHudba;
        } else {
            this.odemcenaHudba = new ArrayList<>(List.of("obchod_theme"));
        }
    }

    public KoupeneHudby zkusKoupitHudbu(String souborHudby, int cena) {
        if (odemcenaHudba.contains(souborHudby)) {
            return KoupeneHudby.VLASTNI;
        }
        if (penize < cena) {
            return KoupeneHudby.NEDOSTATEK_PENEZ;
        }
        penize -= cena;
        odemcenaHudba.add(souborHudby);
        return KoupeneHudby.KOUPENO;
    }


    public int getKomboSerie() {
        return komboSerie;
    }

    public void setKomboSerie(int komboSerie) {
        this.komboSerie = Math.max(0, komboSerie);
        if (this.komboSerie > nejlepsiKombo) nejlepsiKombo = this.komboSerie;
    }

    public void zvysKombo() {
        komboSerie++;
        if (komboSerie > nejlepsiKombo) nejlepsiKombo = komboSerie;
    }

    public void vynulujKombo() {
        komboSerie = 0;
    }

    public double getNasobicKomba() {
        if (komboSerie <= 1) return 1.0;
        double nasobic = 1.0 + 0.10 * (komboSerie - 1);
        return Math.min(nasobic, 2.0);
    }

    public int getCelkemObslouzeno() { return celkemObslouzeno; }
    public void setCelkemObslouzeno(int v) { this.celkemObslouzeno = Math.max(0, v); }

    public int getNejlepsiKombo() { return nejlepsiKombo; }
    public void setNejlepsiKombo(int v) { this.nejlepsiKombo = Math.max(0, v); }

    public int getNejlepsiTrzbaSmeny() { return nejlepsiTrzbaSmeny; }
    public void setNejlepsiTrzbaSmeny(int v) { this.nejlepsiTrzbaSmeny = Math.max(0, v); }

    public int getCelkemVydelano() { return celkemVydelano; }
    public void setCelkemVydelano(int v) { this.celkemVydelano = Math.max(0, v); }

    public int getCilSmeny() { return cilSmeny; }
    public void setCilSmeny(int v) { this.cilSmeny = Math.max(0, v); }

    public int getTrzbaSmeny() { return trzbaSmeny; }
    public void setTrzbaSmeny(int v) { this.trzbaSmeny = Math.max(0, v); }

    public int getCisloSmeny() { return cisloSmeny; }
    public void setCisloSmeny(int v) { this.cisloSmeny = Math.max(1, v); }

    public void zahajNovouSmenu() {
        cilSmeny = vypoctiCilSmeny(cisloSmeny, getLevel());
        trzbaSmeny = 0;
    }

    /**
     * Obnoví rozehranou směnu z uložené hry (bez nulování tržby).
     * Pokud uložený cíl chybí (starší uložená hra, cíl <= 0), dopočítá se
     * z čísla směny a aktuálního levelu. Tržba se zastropuje na nezáporné číslo.
     */
    public void obnovRozehranouSmenu(int ulozenaTrzba, int ulozenyCil) {
        trzbaSmeny = Math.max(0, ulozenaTrzba);
        cilSmeny = (ulozenyCil > 0) ? ulozenyCil : vypoctiCilSmeny(cisloSmeny, getLevel());
    }

    public static int vypoctiCilSmeny(int cisloSmeny, int level) {
        int zaklad = 800 + (cisloSmeny - 1) * 400;
        zaklad += (level - 1) * 150;
        return Math.max(500, zaklad);
    }

    public boolean jeCilSmenySplnen() {
        return cilSmeny > 0 && trzbaSmeny >= cilSmeny;
    }

    public boolean zaevidujNakup(int castka) {
        boolean cilBylSplnen = jeCilSmenySplnen();
        trzbaSmeny += Math.max(0, castka);
        celkemVydelano += Math.max(0, castka);
        celkemObslouzeno++;
        if (trzbaSmeny > nejlepsiTrzbaSmeny) nejlepsiTrzbaSmeny = trzbaSmeny;
        return !cilBylSplnen && jeCilSmenySplnen();
    }
}
