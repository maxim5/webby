package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AtomicLazyRecycle<T> extends AtomicLazyInit<T> implements LazyRecycle<T> {
    protected AtomicLazyRecycle(@Nullable T initValue) {
        super(initValue);
    }

    public static <T> @NotNull AtomicLazyRecycle<T> createUninitialized() {
        return new AtomicLazyRecycle<>(null);
    }

    @Override
    public void recycle() {
        ref.set(null);
    }
}
