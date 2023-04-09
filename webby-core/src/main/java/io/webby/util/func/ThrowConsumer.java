package io.webby.util.func;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowConsumer<T, E extends Throwable> {
    void accept(T t) throws E;

    default @NotNull ThrowConsumer<T, E> andThen(@NotNull ThrowConsumer<? super T, E> after) {
        return (T t) -> { accept(t); after.accept(t); };
    }
}
