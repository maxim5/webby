package io.webby.db.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Random;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public interface LongIdGenerator extends Supplier<Long> {
    long nextId();

    @Override
    default @NotNull Long get() {
        return nextId();
    }

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

    static LongIdGenerator secureRandom(@NotNull SecureRandom random) {
        return random::nextLong;
    }

    static LongIdGenerator securePositiveRandom(@NotNull SecureRandom random) {
        return () -> random.nextLong() & Long.MAX_VALUE;
    }
}
