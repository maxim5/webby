package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResettableAtomicLazy<T> extends AtomicLazy<T> {
    public ResettableAtomicLazy(@Nullable T initValue) {
        super(initValue);
    }

    public ResettableAtomicLazy() {
    }

    public void reset() {
        ref.set(null);
    }

    public void reinitializeOrDie(@NotNull T value) {
        reset();
        initializeOrDie(value);
    }
}
