package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Same as {@link AtomicLazyInit}, but also allows to {@link #reset()} the state.
 */
public class ResettableAtomicLazyInit<T> extends AtomicLazyInit<T> {
    protected ResettableAtomicLazyInit(@Nullable T initValue) {
        super(initValue);
    }

    public static <T> @NotNull ResettableAtomicLazyInit<T> create() {
        return new ResettableAtomicLazyInit<>(null);
    }

    public void reset() {
        ref.set(null);
    }
}
