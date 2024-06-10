package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Allows to store a lazy boolean value. The clients should only provide a way to supply the way.
 * <p>
 * Design note: the API is most convenient for flag cache/memoize use case, e.g.
 * when a flag should be computed once and used multiple times. With boolean an uninitialized value does not exist.
 */
public class LazyBoolean {
    private final BooleanSupplier supplier;
    private boolean computed;
    private boolean value;

    public LazyBoolean(@NotNull BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    public boolean get() {
        if (!computed) {
            value = supplier.getAsBoolean();
            computed = true;
        }
        return value;
    }
}
