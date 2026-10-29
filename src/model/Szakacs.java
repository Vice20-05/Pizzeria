package model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer: takes the next order, "bakes" it, puts it on the counter.
 * put() blocks when the counter is full -> the cook waits, no wait/notify needed.
 */
public class Szakacs implements Runnable {

    private static final String[] TIPUSOK = { "Margherita", "Prosciutto", "Quattro Formaggi", "Diavola", "Hawaii" };

    private final BlockingQueue<Pizza> pult;
    private final AtomicInteger rendelesSzamlalo; // shared by all cooks: next order number
    private final int osszRendeles;
    private final Statisztika stat;

    public Szakacs(BlockingQueue<Pizza> pult, AtomicInteger rendelesSzamlalo, int osszRendeles, Statisztika stat) {
        this.pult = pult;
        this.rendelesSzamlalo = rendelesSzamlalo;
        this.osszRendeles = osszRendeles;
        this.stat = stat;
    }

    @Override
    public void run() {
        try {
            int id;
            // incrementAndGet is atomic -> two cooks never get the same order
            while ((id = rendelesSzamlalo.incrementAndGet()) <= osszRendeles) {
                Thread.sleep(ThreadLocalRandom.current().nextInt(5, 21)); // baking: 5-20 ms
                String tipus = TIPUSOK[id % TIPUSOK.length];
                pult.put(new Pizza(id, tipus, System.nanoTime()));
                stat.elkeszult();
            }
        } catch (InterruptedException e) {
            // don't swallow it: set the flag back so whoever runs us knows
            Thread.currentThread().interrupt();
        }
    }
}
