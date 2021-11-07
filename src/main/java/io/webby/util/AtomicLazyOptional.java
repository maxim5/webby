package io.webby.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class AtomicLazyOptional<T> {
    private final AtomicReference<Optional<T>> ref = new AtomicReference<>(null);

    public @Nullable T lazyGet(@NotNull Supplier<T> supplier) {
        return AtomicLazy.setIfAbsent(ref, () -> Optional.ofNullable(supplier.get())).orElse(null);
    }
}
