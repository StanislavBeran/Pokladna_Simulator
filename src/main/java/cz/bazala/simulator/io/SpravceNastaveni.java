package cz.bazala.simulator.io;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class SpravceNastaveni {

    public enum Zobrazeni {
        R_1280_720("1280 x 720", 1280, 720, false),
        R_1600_900("1600 x 900", 1600, 900, false),
        R_1920_1080("1920 x 1080", 1920, 1080, false),
        FULLSCREEN("Fullscreen", 0, 0, true);

        public final String popis;
        public final int sirka;
        public final int vyska;
        public final boolean fullscreen;

        Zobrazeni(String popis, int sirka, int vyska, boolean fullscreen) {
            this.popis = popis;
            this.sirka = sirka;
            this.vyska = vyska;
            this.fullscreen = fullscreen;
        }

        public static Zobrazeni zNazvu(String nazev) {
            if (nazev != null) {
                for (Zobrazeni z : values()) {
                    if (z.name().equals(nazev)) return z;
                }
            }
            return R_1280_720;
        }
    }

    private static final File   SOUBOR          = new File("nastaveni.cfg");
    private static final String KLIC_ZOBRAZENI  = "Zobrazeni";
    private static final String KLIC_HLASITOST  = "Hlasitost";

    private static Zobrazeni aktualniZobrazeni = null;
    private static int       aktualniHlasitost = 100;


    public static Zobrazeni getZobrazeni() {
        if (aktualniZobrazeni == null) nacti();
        return aktualniZobrazeni;
    }

    public static void setZobrazeni(Zobrazeni zobrazeni) {
        aktualniZobrazeni = zobrazeni;
        uloz();
    }


    public static int getHlasitost() {
        if (aktualniZobrazeni == null) nacti();
        return aktualniHlasitost;
    }

    /** Uloží novou hlasitost do souboru. */
    public static void setHlasitost(int hlasitost) {
        aktualniHlasitost = Math.max(0, Math.min(100, hlasitost));
        uloz();
    }

    private static void nacti() {
        aktualniZobrazeni = Zobrazeni.R_1280_720;
        aktualniHlasitost = 100;

        if (!SOUBOR.exists() || SOUBOR.length() == 0) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(SOUBOR), StandardCharsets.UTF_8))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (!radek.contains(":")) continue;
                String klic   = radek.substring(0, radek.indexOf(":")).trim();
                String hodnota = radek.substring(radek.indexOf(":") + 1).replace(";", "").trim();
                if (klic.equals(KLIC_ZOBRAZENI)) {
                    aktualniZobrazeni = Zobrazeni.zNazvu(hodnota);
                } else if (klic.equals(KLIC_HLASITOST)) {
                    try { aktualniHlasitost = Math.max(0, Math.min(100, Integer.parseInt(hodnota))); }
                    catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Chyba při čtení nastavení: " + e.getMessage());
        }
    }

    private static void uloz() {
        if (aktualniZobrazeni == null) aktualniZobrazeni = Zobrazeni.R_1280_720;
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(SOUBOR), StandardCharsets.UTF_8))) {
            writer.println(KLIC_ZOBRAZENI + ": " + aktualniZobrazeni.name() + ";");
            writer.println(KLIC_HLASITOST + ": " + aktualniHlasitost + ";");
        } catch (IOException e) {
            System.err.println("Chyba při ukládání nastavení: " + e.getMessage());
        }
    }
}
