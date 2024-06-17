package io.webby.util.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class EasyObjects {
    public static <T> @NotNull T firstNonNull(@Nullable T first, @Nullable T second) {
        return first != null ? first : requireNonNull(second, "Both arguments are null");
    }

    public static <T> @NotNull T firstNonNull(@Nullable T first, @NotNull Supplier<@Nullable T> second) {
        return first != null ? first : requireNonNull(second.get(), "Both arguments are null");
    }

    public static <T> @NotNull T firstNonNull(@NotNull Supplier<@Nullable T> first,
                                              @NotNull Supplier<@Nullable T> second) {
        return firstNonNull(first.get(), second);
    }

    public static <T> @NotNull T firstNonNull(@NotNull Supplier<@Nullable T> first,
                                              @NotNull Supplier<@Nullable T> second,
                                              @NotNull T def) {
        return firstNonNull(firstNonNullIfExist(first, second), def);
    }

    public static <T> @NotNull T firstNonNull(@NotNull Iterable<@NotNull Supplier<@Nullable T>> suppliers) {
        return requireNonNull(firstNonNullIfExist(suppliers, (T) null));
    }

    public static <T> @NotNull T firstNonNull(@NotNull Iterable<@NotNull Supplier<@Nullable T>> suppliers,
                                              @NotNull T def) {
        return requireNonNull(firstNonNullIfExist(suppliers, def));
    }

    public static <T> @Nullable T firstNonNullIfExist(@Nullable T first, @Nullable T second) {
        return first != null ? first : second;
    }

    public static <T> @Nullable T firstNonNullIfExist(@Nullable T first, @NotNull Supplier<@Nullable T> second) {
        return first != null ? first : second.get();
    }

    public static <T> @Nullable T firstNonNullIfExist(@NotNull Supplier<@Nullable T> first,
                                                      @NotNull Supplier<@Nullable T> second) {
        return firstNonNullIfExist(first.get(), second);
    }

    public static <T> @Nullable T firstNonNullIfExist(@NotNull Supplier<@Nullable T> first,
                                                      @NotNull Supplier<@Nullable T> second,
                                                      @Nullable T def) {
        return firstNonNullIfExist(firstNonNullIfExist(first, second), def);
    }

    public static <T> @Nullable T firstNonNullIfExist(@NotNull Iterable<Supplier<@Nullable T>> suppliers) {
        return firstNonNullIfExist(suppliers, (T) null);
    }

    public static <T> @Nullable T firstNonNullIfExist(@NotNull Iterable<Supplier<@Nullable T>> suppliers,
                                                      @Nullable T def) {
        for (Supplier<T> supplier : suppliers) {
            T value = supplier.get();
            if (value != null) {
                return value;
            }
        }
        return def;
    }
}
