package io.webby.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class AtomicLazy<T> {
    private final AtomicReference<T> value;

    public AtomicLazy(@Nullable T initValue) {
        value = new AtomicReference<>(initValue);
    }

    public AtomicLazy() {
        this(null);
    }

    public @NotNull T lazyGet(@NotNull Supplier<T> supplier) {
        return setIfAbsent(value, supplier);
    }

    public static <T> @NotNull T setIfAbsent(@NotNull AtomicReference<T> reference, @NotNull Supplier<T> supplier) {
        T value = reference.get();
        if (value == null) {
            value = supplier.get();
            if (!reference.compareAndSet(null, value)) {
                return reference.get();
            }
        }
        return value;
    }
}
