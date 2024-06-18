package io.spbx.util.testing;

import org.jetbrains.annotations.NotNull;

import java.util.function.*;

import static org.junit.jupiter.api.Assertions.fail;

public class MockFunction {
    public static <U, V> @NotNull Failing<U, V> failing() {
        return new Failing<>();
    }

    public static class Failing<U, V> implements
            Function<U, V>, IntFunction<U>, ToIntFunction<U>,
            LongFunction<U>, ToLongFunction<U>,
            DoubleFunction<U>, ToDoubleFunction<U>,
            Predicate<U> {
        @Override
        public V apply(U value) {
            return fail("Must not be called, but called with " + value);
        }

        @Override
        public U apply(int value) {
            return fail("Must not be called, but called with " + value);
        }

        @Override
        public U apply(long value) {
            return fail("Must not be called, but called with " + value);
        }

        @Override
        public int applyAsInt(U value) {
            return fail("Must not be called, but called with " + value);
        }

        @Override
        public long applyAsLong(U value) {
            return fail("Must not be called, but called with " + value);
        }

        @Override
        public U apply(double value) {
            return fail("Must not be called, but called with " + value);
        }

        @Override
        public double applyAsDouble(U value) {
            return fail("Must not be called, but called with " + value);
        }

        @Override
        public boolean test(U value) {
            return fail("Must not be called, but called with " + value);
        }
    }
}
