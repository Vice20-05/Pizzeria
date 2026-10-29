package model;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe counters. Many cooks and couriers write it at the same time,
 * so plain int++ would lose updates -> Atomic* classes and a concurrent queue.
 */
public class Statisztika {

    private final AtomicInteger elkeszult = new AtomicInteger();
    private final AtomicInteger kiszallitott = new AtomicInteger();
    private final AtomicLong osszVarakozasNanos = new AtomicLong();

    // which pizza ids were delivered (the tests check: each exactly once)
    private final ConcurrentLinkedQueue<Integer> kiszallitottAzonositok = new ConcurrentLinkedQueue<>();

    public void elkeszult() {
        elkeszult.incrementAndGet();
    }

    /** A courier took the pizza from the counter after it waited this long. */
    public void felveve(long varakozasNanos) {
        osszVarakozasNanos.addAndGet(varakozasNanos);
    }

    public void kiszallitva(int pizzaId) {
        kiszallitott.incrementAndGet();
        kiszallitottAzonositok.add(pizzaId);
    }

    public int getElkeszult() {
        return elkeszult.get();
    }

    public int getKiszallitott() {
        return kiszallitott.get();
    }

    /** Average time a pizza waited on the counter, in milliseconds. */
    public double atlagVarakozasMs() {
        int db = kiszallitott.get();
        return db == 0 ? 0 : osszVarakozasNanos.get() / 1_000_000.0 / db;
    }

    public List<Integer> getKiszallitottAzonositok() {
        return List.copyOf(kiszallitottAzonositok);
    }
}
