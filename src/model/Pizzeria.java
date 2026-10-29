package model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs one simulation: starts the cooks and couriers, waits until every
 * pizza is delivered, then shuts everything down cleanly.
 */
public class Pizzeria {

    private static final long MAX_VARAKOZAS_PERC = 1;

    public Eredmeny futtat(Konfiguracio k) throws InterruptedException {
        BlockingQueue<Pizza> pult = new LinkedBlockingQueue<>(k.pultMeret()); // bounded counter
        Statisztika stat = new Statisztika();
        AtomicInteger rendelesSzamlalo = new AtomicInteger(0);

        ExecutorService szakacsPool = Executors.newFixedThreadPool(k.szakacsok());
        ExecutorService futarPool = Executors.newFixedThreadPool(k.futarok());

        long kezdes = System.nanoTime();
        for (int i = 0; i < k.futarok(); i++) {
            futarPool.submit(new Futar(pult, stat));
        }
        for (int i = 0; i < k.szakacsok(); i++) {
            szakacsPool.submit(new Szakacs(pult, rendelesSzamlalo, k.rendelesek(), stat));
        }

        // 1) no new tasks for the cooks, wait until all orders are baked
        szakacsPool.shutdown();
        if (!szakacsPool.awaitTermination(MAX_VARAKOZAS_PERC, TimeUnit.MINUTES)) {
            szakacsPool.shutdownNow();
        }

        // 2) one poison pill per courier. They're behind the real pizzas in the
        //    queue, so every pizza is delivered before the couriers stop.
        for (int i = 0; i < k.futarok(); i++) {
            pult.put(Pizza.MEREGPIRULA);
        }
        futarPool.shutdown();
        if (!futarPool.awaitTermination(MAX_VARAKOZAS_PERC, TimeUnit.MINUTES)) {
            futarPool.shutdownNow();
        }
        long osszIdoMs = (System.nanoTime() - kezdes) / 1_000_000;

        return new Eredmeny(k, stat.getElkeszult(), stat.getKiszallitott(),
                stat.atlagVarakozasMs(), osszIdoMs, stat.getKiszallitottAzonositok());
    }
}
