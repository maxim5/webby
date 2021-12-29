package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface DelayedAccessLazy<T> {
    default @NotNull T lazyGet(@NotNull T value) {
        return lazyGet(() -> value);
    }

    @NotNull T lazyGet(@NotNull Supplier<T> supplier);
}
