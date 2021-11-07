package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

public interface DelayedInitLazy<T> {
    boolean isInitialized();

    void initializeOrDie(@NotNull T value);

    @NotNull T getOrDie();
}
