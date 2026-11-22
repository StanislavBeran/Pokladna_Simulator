package cz.pokladna.simulator.model;

public enum TypZbozi {
    KUSOVE(0, "Má kód"),
    PECIVO(1, "Pečivo"),
    ZELENINA(2, "Zelenina"),
    OVOCE(3, "Ovoce"),
    OSTATNI(4, "Ostatní");

    public static final int KATEGORIE_VSE = 5;

    private final int kod;
    private final String nazev;

    TypZbozi(int kod, String nazev) {
        this.kod = kod;
        this.nazev = nazev;
    }

    public int getKod() {
        return kod;
    }

    public String getNazev() {
        return nazev;
    }

    public static TypZbozi zKodu(int kod) {
        for (TypZbozi t : values()) {
            if (t.kod == kod) return t;
        }
        return KUSOVE;
    }

    public static boolean jeSkenovane(int kod) {
        return kod >= PECIVO.kod && kod <= OSTATNI.kod;
    }

    public static boolean jeKusove(int kod) {
        return kod == KUSOVE.kod;
    }
}
