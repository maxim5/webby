package io.spbx.webby.benchmarks.stress;

import java.util.function.Supplier;

public class RandomIntGenerator extends Rand implements Supplier<Integer> {
    private final int max;

    public RandomIntGenerator(int max) {
        this.max = max;
    }

    public RandomIntGenerator(long seed, int max) {
        super(seed);
        this.max = max;
    }

    @Override
    public Integer get() {
        return (int) (random() * max);
    }
}
