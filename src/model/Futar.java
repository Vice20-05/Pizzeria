package model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Consumer: takes a pizza from the counter and delivers it.
 * take() blocks when the counter is empty. It stops when it gets the poison pill.
 */
public class Futar implements Runnable {

    private final BlockingQueue<Pizza> pult;
    private final Statisztika stat;

    public Futar(BlockingQueue<Pizza> pult, Statisztika stat) {
        this.pult = pult;
        this.stat = stat;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Pizza pizza = pult.take();
                if (pizza.isMeregpirula()) {
                    return; // clean stop, no stop() and no endless loop
                }
                stat.felveve(System.nanoTime() - pizza.keszIdo());
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 31)); // the ride: 10-30 ms
                stat.kiszallitva(pizza.id());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
