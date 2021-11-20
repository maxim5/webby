package io.webby.util.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ReversibleFunction<U, V> extends Function<U, V> {
    @Override
    @Nullable V apply(@Nullable U u);

    @NotNull V applyNotNull(@NotNull U u);

    @NotNull ReversibleFunction<V, U> reverse();
}
