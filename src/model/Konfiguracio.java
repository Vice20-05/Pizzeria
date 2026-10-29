package model;

/**
 * The parameters of one simulation run.
 */
public record Konfiguracio(int szakacsok, int futarok, int pultMeret, int rendelesek) {

    // compact constructor: runs before the fields are set -> validation
    public Konfiguracio {
        if (szakacsok <= 0 || futarok <= 0 || pultMeret <= 0 || rendelesek <= 0) {
            throw new IllegalArgumentException("Minden paraméternek pozitívnak kell lennie.");
        }
    }
}
