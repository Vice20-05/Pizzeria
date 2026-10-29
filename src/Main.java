import java.util.List;

import model.Eredmeny;
import model.Konfiguracio;
import model.Pizzeria;

/**
 * Pizzeria producer-consumer simulation.
 * <p>
 * Without arguments: runs several configurations and prints a comparison table.
 * With arguments: runs only the given one, e.g.
 * {@code java -cp out Main --szakacs 3 --futar 4 --pult 10 --rendeles 200}
 */
public class Main {

    public static void main(String[] args) {
        try {
            List<Konfiguracio> konfigok = args.length == 0 ? osszehasonlitas() : List.of(beolvas(args));

            Pizzeria pizzeria = new Pizzeria();
            System.out.println("Szakács | Futár | Pult | Rendelés | Kiszállítva | Átl. várakozás a pulton | Összidő");
            System.out.println("--------+-------+------+----------+-------------+-------------------------+--------");
            for (Konfiguracio k : konfigok) {
                Eredmeny e = pizzeria.futtat(k);
                System.out.printf("%7d | %5d | %4d | %8d | %11d | %20.1f ms | %4d ms%n",
                        k.szakacsok(), k.futarok(), k.pultMeret(), k.rendelesek(),
                        e.kiszallitott(), e.atlagVarakozasMs(), e.osszIdoMs());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Hibás paraméter: " + e.getMessage());
            System.out.println("Használat: java -cp out Main [--szakacs N] [--futar N] [--pult N] [--rendeles N]");
        } catch (InterruptedException e) {
            System.out.println("A szimulációt megszakították.");
            Thread.currentThread().interrupt();
        }
    }

    /** The configurations for the README table: always change one thing compared to the first row. */
    private static List<Konfiguracio> osszehasonlitas() {
        return List.of(
                new Konfiguracio(3, 4, 10, 200),   // base: the Thursday evening from the task
                new Konfiguracio(1, 4, 10, 200),   // fewer cooks
                new Konfiguracio(6, 4, 10, 200),   // more cooks
                new Konfiguracio(3, 2, 10, 200),   // fewer couriers
                new Konfiguracio(3, 8, 10, 200),   // more couriers
                new Konfiguracio(6, 8, 10, 200),   // both more
                new Konfiguracio(3, 4, 1, 200));   // tiny counter
    }

    /** --szakacs 3 --futar 4 ... -> Konfiguracio. Missing ones get the default (3, 4, 10, 200). */
    private static Konfiguracio beolvas(String[] args) {
        int szakacsok = 3, futarok = 4, pult = 10, rendelesek = 200;
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("hiányzik az érték ehhez: " + args[i]);
            }
            int ertek = szam(args[i + 1]);
            switch (args[i]) {
                case "--szakacs" -> szakacsok = ertek;
                case "--futar" -> futarok = ertek;
                case "--pult" -> pult = ertek;
                case "--rendeles" -> rendelesek = ertek;
                default -> throw new IllegalArgumentException("ismeretlen kapcsoló: " + args[i]);
            }
        }
        return new Konfiguracio(szakacsok, futarok, pult, rendelesek);
    }

    private static int szam(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("nem szám: " + s);
        }
    }
}
