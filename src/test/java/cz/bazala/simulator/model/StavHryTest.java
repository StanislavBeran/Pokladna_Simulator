package cz.bazala.simulator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StavHryTest {

    @Test
    void levelZacinaNaJedneARosteSXp() {
        StavHry s = new StavHry();
        assertEquals(1, s.getLevel());

        s.setCelkoveXp(100);
        assertEquals(2, s.getLevel());
        assertEquals(0, s.getXpDoDalsihoLevelu());

        s.setCelkoveXp(250);
        assertEquals(3, s.getLevel());
        assertEquals(50, s.getXpDoDalsihoLevelu());
    }

    @Test
    void odemkniZboziPridaNoveAleNeDuplikuje() {
        StavHry s = new StavHry();
        assertTrue(s.odemkniZbozi(42));
        assertTrue(s.getOdemceneZbozi().contains(42));

        assertFalse(s.odemkniZbozi(42));
    }

    @Test
    void koupeHudbyStrhneCenuAPridaJi() {
        StavHry s = new StavHry();
        s.setPenize(1000);

        assertEquals(StavHry.VysledekKoupeHudby.KOUPENO, s.zkusKoupitHudbu("pisnicka", 600));
        assertEquals(400, s.getPenize());
        assertTrue(s.getOdemcenaHudba().contains("pisnicka"));

        assertEquals(StavHry.VysledekKoupeHudby.JIZ_VLASTNENO, s.zkusKoupitHudbu("pisnicka", 600));
        assertEquals(400, s.getPenize());
    }

    @Test
    void koupeHudbyPriNedostatkuPenezNeprojde() {
        StavHry s = new StavHry();
        s.setPenize(100);

        assertEquals(StavHry.VysledekKoupeHudby.NEDOSTATEK_PENEZ, s.zkusKoupitHudbu("drahapisen", 600));
        assertEquals(100, s.getPenize());
        assertFalse(s.getOdemcenaHudba().contains("drahapisen"));
    }

    @Test
    void rychlostPasuRosteAZastaviSeNaMaximu() {
        StavHry s = new StavHry();
        assertEquals(0, s.getUrovenRychlostiPasu());

        for (int ocekavana = 1; ocekavana <= 4; ocekavana++) {
            assertEquals(ocekavana, s.zvysUrovenRychlostiPasu());
        }
        assertEquals(-1, s.zvysUrovenRychlostiPasu());
        assertEquals(4, s.getUrovenRychlostiPasu());
    }

    @Test
    void prepinatelneVylepseniMaMaximumJedna() {
        StavHry s = new StavHry();
        assertFalse(s.maDruhyScanner());

        assertEquals(1, s.zvysUrovenVylepseni(StavHry.Vylepseni.DRUHY_SCANNER));
        assertTrue(s.maDruhyScanner());

        assertEquals(-1, s.zvysUrovenVylepseni(StavHry.Vylepseni.DRUHY_SCANNER));
    }

    @Test
    void nasobiceOdmenATrpelivostiRostouSUrovni() {
        StavHry s = new StavHry();
        assertEquals(1.0, s.getNasobicOdmen(), 1e-9);
        assertEquals(1.0, s.getNasobicTrpelivosti(), 1e-9);

        s.zvysUrovenVylepseni(StavHry.Vylepseni.BONUS_ODMEN);
        s.zvysUrovenVylepseni(StavHry.Vylepseni.TRPELIVOST);

        assertEquals(1.15, s.getNasobicOdmen(), 1e-9);
        assertEquals(1.30, s.getNasobicTrpelivosti(), 1e-9);
    }

    @Test
    void setVylepseniNormalizujeDelkuAVstup() {
        StavHry s = new StavHry();
        s.setVylepseni("4x");
        assertEquals(StavHry.POCET_VYLEPSENI, s.getVylepseni().length());
        assertEquals(4, s.getUrovenRychlostiPasu());
    }
    
    @Test
    void komboRosteANasobicSeStropuje() {
        StavHry s = new StavHry();
        assertEquals(1.0, s.getNasobicKomba(), 1e-9);

        s.zvysKombo(); // serie 1 -> násobič stále 1.0
        assertEquals(1.0, s.getNasobicKomba(), 1e-9);

        s.zvysKombo(); // serie 2 -> 1.1
        assertEquals(1.1, s.getNasobicKomba(), 1e-9);

        for (int i = 0; i < 50; i++) s.zvysKombo();
        assertEquals(2.0, s.getNasobicKomba(), 1e-9); // strop
        assertTrue(s.getNejlepsiKombo() >= s.getKomboSerie());
    }

    @Test
    void vynulovaniKombaNesnizujeRekord() {
        StavHry s = new StavHry();
        s.zvysKombo();
        s.zvysKombo();
        s.zvysKombo();
        int rekord = s.getNejlepsiKombo();
        assertEquals(3, rekord);

        s.vynulujKombo();
        assertEquals(0, s.getKomboSerie());
        assertEquals(rekord, s.getNejlepsiKombo());
    }

    @Test
    void cilSmenyRosteSCislemSmenyALevelem() {
        int cil1 = StavHry.vypoctiCilSmeny(1, 1);
        int cil2 = StavHry.vypoctiCilSmeny(2, 1);
        int cilVyssiLevel = StavHry.vypoctiCilSmeny(1, 5);

        assertTrue(cil2 > cil1);
        assertTrue(cilVyssiLevel > cil1);
        assertTrue(cil1 >= 500);
    }

    @Test
    void zaevidovaniNakupuDetekujeSplneniCile() {
        StavHry s = new StavHry();
        s.setCisloSmeny(1);
        s.zahajNovouSmenu();
        int cil = s.getCilSmeny();
        assertTrue(cil > 0);
        assertFalse(s.jeCilSmenySplnen());

        boolean splnenoHned = s.zaevidujNakup(cil - 1);
        assertFalse(splnenoHned);
        assertFalse(s.jeCilSmenySplnen());

        boolean splneno = s.zaevidujNakup(10);
        assertTrue(splneno);
        assertTrue(s.jeCilSmenySplnen());

        assertFalse(s.zaevidujNakup(100));
    }

    @Test
    void zaevidovaniNakupuAktualizujeStatistiky() {
        StavHry s = new StavHry();
        s.zahajNovouSmenu();

        s.zaevidujNakup(300);
        s.zaevidujNakup(200);

        assertEquals(2, s.getCelkemObslouzeno());
        assertEquals(500, s.getCelkemVydelano());
        assertEquals(500, s.getTrzbaSmeny());
        assertEquals(500, s.getNejlepsiTrzbaSmeny());
    }
}
