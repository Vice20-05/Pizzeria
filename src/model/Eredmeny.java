package model;

import java.util.List;

/** What one simulation run measured. */
public record Eredmeny(Konfiguracio konfig, int elkeszult, int kiszallitott,
        double atlagVarakozasMs, long osszIdoMs, List<Integer> kiszallitottAzonositok) {
}
