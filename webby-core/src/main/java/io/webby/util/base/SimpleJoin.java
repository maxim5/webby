package io.webby.util.base;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class SimpleJoin {
    private final List<String> items;

    private SimpleJoin(@NotNull List<String> items) {
        this.items = items;
    }

    public static @NotNull SimpleJoin of(@NotNull String item) {
        return new SimpleJoin(List.of(item));
    }

    public static @NotNull SimpleJoin of(@NotNull String item1, @NotNull String item2) {
        return new SimpleJoin(List.of(item1, item2));
    }

    public static @NotNull SimpleJoin of(@NotNull String @NotNull ... items) {
        return new SimpleJoin(List.of(items));
    }

    public static @NotNull SimpleJoin of(@NotNull Object item) {
        return new SimpleJoin(List.of(item.toString()));
    }

    public static @NotNull SimpleJoin of(@NotNull Object item1, @NotNull Object item2) {
        return new SimpleJoin(List.of(item1.toString(), item2.toString()));
    }

    public static @NotNull SimpleJoin of(@NotNull Object @NotNull ... items) {
        return new SimpleJoin(Stream.of(items).map(Object::toString).toList());
    }

    public static @NotNull SimpleJoin from(@NotNull Collection<?> pieces) {
        return new SimpleJoin(pieces.stream().map(Object::toString).toList());
    }

    @CheckReturnValue
    public @NotNull SimpleJoin onlyNonEmpty() {
        return new SimpleJoin(items.stream().filter(s -> !s.isEmpty()).toList());
    }

    @CheckReturnValue
    public @NotNull String join() {
        return String.join("", items);
    }

    @CheckReturnValue
    public @NotNull String join(@NotNull String separator) {
        return String.join(separator, items);
    }

    @Override
    public String toString() {
        return join();
    }
}
