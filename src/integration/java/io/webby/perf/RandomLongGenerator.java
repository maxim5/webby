package io.webby.perf;

import java.util.function.Supplier;

public class RandomLongGenerator extends Rand implements Supplier<Long> {
    private final long max;

    public RandomLongGenerator(long max) {
        this.max = max;
    }

    public RandomLongGenerator(long seed, long max) {
        super(seed);
        this.max = max;
    }

    @Override
    public Long get() {
        return (long) (random() * max);
    }
}
