package io.spbx.webby.benchmarks.stress;

import java.util.Random;

public class Rand {
    private final long seed;
    private final Random random;

    public Rand() {
        this(8682522807148012L ^ System.nanoTime());
    }

    public Rand(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public long seed() {
        return seed;
    }

    public double random() {
        return random.nextDouble();
    }

    public int randomInt(int max) {
        return random.nextInt(max);
    }
}
