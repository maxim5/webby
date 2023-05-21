package io.webby.db.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Random;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface IntIdGenerator extends Supplier<Integer> {
    int nextId();

    @Override
    default @NotNull Integer get() {
        return nextId();
    }

    static IntIdGenerator autoIncrement(@NotNull IntSupplier sizeSupplier) {
        return () -> sizeSupplier.getAsInt() + 1;
    }

    static IntIdGenerator random(@Nullable Integer seed) {
        Random random = seed == null ? new Random() : new Random(seed);
        return random::nextInt;
    }

    static IntIdGenerator positiveRandom(@Nullable Integer seed) {
        Random random = seed == null ? new Random() : new Random(seed);
        return () -> random.nextInt() & Integer.MAX_VALUE;
    }

    static IntIdGenerator secureRandom(@NotNull SecureRandom random) {
        return random::nextInt;
    }

    static IntIdGenerator securePositiveRandom(@NotNull SecureRandom random) {
        return () -> random.nextInt() & Integer.MAX_VALUE;
    }
}
