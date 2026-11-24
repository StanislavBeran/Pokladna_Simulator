package cz.pokladna.simulator.model;

public final class SouhrnSmeny {
    public final int cisloSmeny;
    public final int trzba;
    public final int cil;
    public final int zakaznici;
    public final int spokojeni;
    public final int nastvani;
    public final int chyby;
    public final int nejlepsiKombo;
    public final int hvezdy;
    public final int bonus;
    public final double nasobicBonusu;

    public SouhrnSmeny(int cisloSmeny, int trzba, int cil, int zakaznici, int spokojeni, int nastvani,
                       int chyby, int nejlepsiKombo, int hvezdy, int bonus, double nasobicBonusu) {
        this.cisloSmeny = cisloSmeny;
        this.trzba = trzba;
        this.cil = cil;
        this.zakaznici = zakaznici;
        this.spokojeni = spokojeni;
        this.nastvani = nastvani;
        this.chyby = chyby;
        this.nejlepsiKombo = nejlepsiKombo;
        this.hvezdy = hvezdy;
        this.bonus = bonus;
        this.nasobicBonusu = nasobicBonusu;
    }
}
