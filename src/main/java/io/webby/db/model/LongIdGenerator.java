package io.webby.db.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.LongSupplier;

public interface LongIdGenerator {
    long nextId();

    static LongIdGenerator autoIncrement(@NotNull LongSupplier sizeSupplier) {
        return () -> sizeSupplier.getAsLong() + 1;
    }

    static LongIdGenerator random(@Nullable Integer seed) {
        Random random = seed == null ? new Random() : new Random(seed);
        return random::nextLong;
    }

    static LongIdGenerator positiveRandom(@Nullable Integer seed) {
        Random random = seed == null ? new Random() : new Random(seed);
        return () -> random.nextLong() & Long.MAX_VALUE;
    }
}
