# Termelő–fogyasztó: pizzéria-szimuláció (b-10)

Cooks (producers) bake pizzas and put them on a bounded warming counter, couriers
(consumers) take them off and deliver them. The counter is a
`LinkedBlockingQueue<Pizza>`, the threads run in `ExecutorService`s, and the
couriers stop with a **poison pill**.

## Requirements

- Java JDK 17 or newer (uses `record` and `switch ->`)

## Project structure

```
Pizzeria/
├── src/
│   ├── Main.java            -> runs the comparison table (or one config from the arguments)
│   └── model/
│       ├── Pizza.java          -> immutable record + MEREGPIRULA (poison pill)
│       ├── Szakacs.java        -> producer (Runnable), bakes 5-20 ms, put()
│       ├── Futar.java          -> consumer (Runnable), take(), rides 10-30 ms
│       ├── Statisztika.java    -> AtomicInteger / AtomicLong counters
│       ├── Pizzeria.java       -> starts the threads, clean shutdown
│       ├── Konfiguracio.java   -> record: cooks, couriers, counter size, orders
│       └── Eredmeny.java       -> record: what one run measured
├── test/model/
│   └── PizzeriaTest.java    -> 8 JUnit 5 tests (assertTimeoutPreemptively)
├── lib/                     -> junit-platform-console-standalone-6.1.3.jar
├── docs/
│   └── pizzeria.puml        -> PlantUML sequence diagram of the threads
└── README.md
```

## How to run

```bash
javac -d out src/Main.java src/model/*.java
java -cp out Main                                   # comparison table
java -cp out Main --szakacs 3 --futar 4 --pult 10 --rendeles 200   # one config
```

Any switch you leave out gets its default (3 cooks, 4 couriers, counter 10, 200 orders).

## How to run the tests

```bash
javac -d out src/Main.java src/model/*.java
javac -d out_test -cp out:lib/junit-platform-console-standalone-6.1.3.jar test/model/*.java
java -jar lib/junit-platform-console-standalone-6.1.3.jar \
  execute --class-path out:out_test --scan-class-path
```

## How it works

- **No wait/notify:** `put()` blocks when the counter is full (the cook waits), `take()` blocks when it's empty (the courier waits).
- **No order twice:** the cooks share one `AtomicInteger`, `incrementAndGet()` hands out 1, 2, 3... and nobody gets the same number.
- **Clean stop:** after `shutdown()` + `awaitTermination()` on the cooks, `Pizzeria` puts **one poison pill per courier** on the counter. They sit behind the real pizzas, so every pizza goes out before the couriers stop. No `stop()`, no endless loop.
- **InterruptedException** isn't swallowed: the flag is set back with `Thread.currentThread().interrupt()`.
- **Waiting time** = from the moment the pizza is ready (`keszIdo`, before `put()`) until a courier takes it.

## Measurements

200 orders each, my machine (the numbers jump a few % between runs because of `sleep` and scheduling):

| Cooks | Couriers | Counter | Avg. wait on the counter | Total time |
|------:|---------:|--------:|-------------------------:|-----------:|
| 3     | 4        | 10      | 39.9 ms                  | 1006 ms    |
| 1     | 4        | 10      | 0.1 ms                   | 2638 ms    |
| 6     | 4        | 10      | 62.5 ms                  | 1011 ms    |
| 3     | 2        | 10      | 112.3 ms                 | 2043 ms    |
| 3     | 8        | 10      | 0.1 ms                   | 845 ms     |
| 6     | 8        | 10      | 19.9 ms                  | 523 ms     |
| 3     | 4        | 1       | 6.3 ms                   | 1027 ms    |

**What it shows:** in the base case the **couriers are the bottleneck**. 4 couriers
with ~20 ms rides do about 0.2 pizza/ms, the 3 cooks with ~12.5 ms baking do
0.24 pizza/ms, so 200 pizzas take ~1000 ms and the pizzas pile up on the counter
(40 ms wait). That's why 6 cooks don't help at all: the total time stays ~1000 ms,
the pizzas just wait longer. 2 couriers double the time, while 8 couriers make
the **cooks** the bottleneck (845 ms ≈ 200 / 0.24) and the pizzas are picked up
right away. Making both sides bigger is what really helps (523 ms). A counter of 1
doesn't change the speed, only where the waiting happens: the cooks stand
blocked in `put()` instead of the pizzas cooling on the counter.

## UML diagram

`docs/pizzeria.puml` is a **sequence diagram** of how the threads cooperate
through the counter, including the shutdown with the poison pills. Open it with
the PlantUML VS Code extension or paste it into https://www.plantuml.com/plantuml.

## Note

Claude only checked if the folders have README and gitignore and pushed them so all of them are pushed
