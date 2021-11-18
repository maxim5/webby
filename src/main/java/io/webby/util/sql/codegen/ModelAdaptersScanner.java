package io.webby.util.sql.codegen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModelAdaptersScanner {
    @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model);

    @NotNull FQN locateAdapterFqn(@NotNull Class<?> model);
}
