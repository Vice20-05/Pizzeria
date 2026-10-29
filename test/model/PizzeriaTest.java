package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for the producer-consumer simulation.
 * Every run is inside assertTimeoutPreemptively: if there were a deadlock,
 * the test would fail after a few seconds instead of hanging forever.
 */
public class PizzeriaTest {

    private static final Duration MAX_IDO = Duration.ofSeconds(10);

    private final Pizzeria pizzeria = new Pizzeria();

    private Eredmeny futtat(int szakacsok, int futarok, int pult, int rendelesek) {
        return assertTimeoutPreemptively(MAX_IDO,
                () -> pizzeria.futtat(new Konfiguracio(szakacsok, futarok, pult, rendelesek)));
    }

    @Test
    public void everyPizzaIsDeliveredExactlyOnce() {
        Eredmeny e = futtat(3, 4, 10, 100);
        List<Integer> azonositok = e.kiszallitottAzonositok();

        assertEquals(100, azonositok.size());
        Set<Integer> egyedi = new HashSet<>(azonositok);
        assertEquals(100, egyedi.size(), "duplicated pizza");
        for (int id = 1; id <= 100; id++) {
            assertTrue(egyedi.contains(id), "lost pizza: " + id);
        }
    }

    @Test
    public void countersMatchTheOrders() {
        Eredmeny e = futtat(3, 4, 10, 80);
        assertEquals(80, e.elkeszult());
        assertEquals(80, e.kiszallitott());
    }

    @Test
    public void poisonPillIsNotDelivered() {
        Eredmeny e = futtat(2, 3, 10, 20);
        assertFalse(e.kiszallitottAzonositok().contains(Pizza.MEREGPIRULA.id()));
    }

    @Test
    public void counterOfSizeOneDoesNotDeadlock() {
        Eredmeny e = futtat(4, 4, 1, 60);
        assertEquals(60, e.kiszallitott());
    }

    @Test
    public void moreCouriersThanPizzasStillStops() {
        // most couriers only ever get a poison pill
        Eredmeny e = futtat(1, 10, 10, 3);
        assertEquals(3, e.kiszallitott());
    }

    @Test
    public void manyCooksOneCourier() {
        Eredmeny e = futtat(8, 1, 5, 30);
        assertEquals(30, e.kiszallitott());
    }

    @Test
    public void poisonPillIsRecognisedOnlyByIdentity() {
        Pizza hasonmas = new Pizza(-1, "STOP", 0);   // same fields, different object
        assertTrue(Pizza.MEREGPIRULA.isMeregpirula());
        assertFalse(hasonmas.isMeregpirula());
    }

    @Test
    public void invalidConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Konfiguracio(0, 4, 10, 200));
        assertThrows(IllegalArgumentException.class, () -> new Konfiguracio(3, 4, -1, 200));
    }
}
