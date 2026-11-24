# Pokladna Simulator

Absolventská práce: simulátor pokladní v obchodě, napsaný v Javě (Swing).

Zákazníci posílají zboží po pásu a ty ho markuješ, zadáváš kódy, vybíráš platbu kartou nebo hotově a vracíš peníze. Za každou směnu dostaneš hodnocení a bonus. Za vydělané peníze si v obchodě koupíš vylepšení a hudbu.

## Funkce

- interaktivní tutoriál pro první směnu
- pás se zbožím, pokladní displej s vyhledáváním zboží a vracení peněz
- karta zákazníka s náhodnou fotkou, ukazatelem spokojenosti a místem pro chat
- souhrn na konci směny s hvězdami (1–3), bonusem a nově odemčeným zbožím
- obchod s vylepšeními a hudbou
- ukládání a načítání her, a to i uprostřed rozehrané směny
- statistiky a nastavení (hlasitost se ukládá do `nastaveni.cfg`)

## Ovládání

| Klávesa | Akce |
| --- | --- |
| Mezerník | posune položku na pásu |
| Číslice / numerická klávesnice | zadávání na displej pokladny |
| Enter | potvrzení |
| Backspace | smazání znaku |

Ostatní se ovládá myší.

## Spuštění

Potřebuješ JDK 16 nebo novější.

Přes Maven:

```
mvn compile exec:java -Dexec.mainClass=cz.pokladna.simulator.ui.Menu
```

Nebo z IDE (IntelliJ IDEA / VS Code) spusť třídu `cz.pokladna.simulator.ui.Menu`.

## Struktura

- `cz.pokladna.simulator.model` obsahuje herní logiku a data (stav hry, zboží, souhrn směny)
- `cz.pokladna.simulator.io` řeší ukládání her, nastavení a zvuky
- `cz.pokladna.simulator.ui` obsahuje okna, menu a herní obrazovku
- `src/main/resources` obsahuje obrázky, zvuky a fotky zákazníků (`zakaznici/zakaznikN.png`)
