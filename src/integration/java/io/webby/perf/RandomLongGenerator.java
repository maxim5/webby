package io.webby.perf;

import java.util.Random;
import java.util.function.Supplier;

public class RandomLongGenerator implements Supplier<Long> {
    private final long seed;
    private final Random random;
    private final long maxId;

    public RandomLongGenerator(long maxId) {
        this(8682522807148012L ^ System.nanoTime(), maxId);
    }

    public RandomLongGenerator(long seed, long maxId) {
        this.seed = seed;
        this.random = new Random(seed);
        this.maxId = maxId;
    }

    @Override
    public Long get() {
        return (long) (random.nextDouble() * maxId);
    }

    public long seed() {
        return seed;
    }
}
