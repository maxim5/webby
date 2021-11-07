package io.webby.util;

import org.jetbrains.annotations.NotNull;

public interface DelayedInitLazy<T> {
    boolean isInitialized();

    void initializeOrDie(@NotNull T value);

    @NotNull T getOrDie();
}
