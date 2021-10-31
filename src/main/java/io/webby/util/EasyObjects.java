package io.webby.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class EasyObjects {
    public static <T> @Nullable T firstNonNull(@NotNull Iterable<Supplier<@Nullable T>> suppliers) {
        return firstNonNull(suppliers, null);
    }

    public static <T> @Nullable T firstNonNull(@NotNull Iterable<Supplier<@Nullable T>> suppliers, @Nullable T def) {
        for (Supplier<T> supplier : suppliers) {
            T value = supplier.get();
            if (value != null) {
                return value;
            }
        }
        return def;
    }
}