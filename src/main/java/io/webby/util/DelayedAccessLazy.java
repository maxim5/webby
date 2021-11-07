package io.webby.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface DelayedAccessLazy<T> {
    @NotNull T lazyGet(@NotNull Supplier<T> supplier);
}
