package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Same as {@link AtomicLazy}, but also allows to {@link #reset()} the state.
 */
public class ResettableAtomicLazy<T> extends AtomicLazy<T> {
    protected ResettableAtomicLazy(@Nullable T initValue) {
        super(initValue);
    }

    public static <T> @NotNull ResettableAtomicLazy<T> emptyLazy() {
        return new ResettableAtomicLazy<>(null);
    }

    public void reset() {
        ref.set(null);
    }
}
