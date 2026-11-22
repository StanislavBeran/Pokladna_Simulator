package cz.pokladna.simulator.model;


public final class Rozmery {

    private Rozmery() {
       
    }

    
    public static final int REF_SIRKA = 1280;
    public static final int REF_VYSKA = 720;
    public static final int REF_VYSKA_PASU = 190;

    public static double scaleX(int aktualniSirka) {
        return (aktualniSirka <= 0) ? 1.0 : (double) aktualniSirka / REF_SIRKA;
    }

    public static double scaleY(int aktualniVyska) {
        return (aktualniVyska <= 0) ? 1.0 : (double) aktualniVyska / REF_VYSKA;
    }
}
