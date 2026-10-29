package model;

/**
 * One pizza. A record, so it's immutable: once it's on the counter, nobody can change it
 * (that's what makes it safe to pass between threads).
 *
 * @param id      order number (1, 2, 3...)
 * @param tipus   e.g. "Margherita"
 * @param keszIdo when it was ready, in System.nanoTime() (for the waiting time on the counter)
 */
public record Pizza(int id, String tipus, long keszIdo) {

    /**
     * Poison pill: a special "pizza" that tells a courier to stop.
     * The cooks never make it, only Pizzeria puts it on the counter at the end.
     */
    public static final Pizza MEREGPIRULA = new Pizza(-1, "STOP", 0);

    public boolean isMeregpirula() {
        return this == MEREGPIRULA; // the same object, not just equal fields
    }
}
