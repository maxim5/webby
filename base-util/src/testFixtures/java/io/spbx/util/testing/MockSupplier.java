package io.spbx.util.testing;

import com.google.common.primitives.Booleans;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class MockSupplier<T> implements Supplier<T> {
    private final T[] returns;
    private int pos;

    protected MockSupplier(@Nullable T @NotNull [] returns) {
        this.returns = returns;
    }

    @SafeVarargs
    public static <T> @NotNull MockSupplier<T> mock(@Nullable T @NotNull ... returns) {
        return new MockSupplier<>(returns);
    }

    @Override
    public T get() {
        assert pos < returns.length :
            "MockSupplier is called too many times: %d, returns=%s".formatted(pos + 1, Arrays.toString(returns));
        return returns[pos++];
    }

    public int timesCalled() {
        return pos;
    }

    public static class Bool extends MockSupplier<Boolean> implements BooleanSupplier {
        protected Bool(boolean[] returns) {
            super(Booleans.asList(returns).toArray(Boolean[]::new));
        }

        public static @NotNull MockSupplier.Bool of(boolean ... returns) {
            return new MockSupplier.Bool(returns);
        }

        @Override
        public boolean getAsBoolean() {
            return get();
        }
    }
}
