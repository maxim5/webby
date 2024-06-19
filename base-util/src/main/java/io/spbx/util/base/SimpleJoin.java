package io.spbx.util.base;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Immutable
public class SimpleJoin {
    private final ImmutableList<String> items;

    private SimpleJoin(@NotNull List<String> items) {
        this.items = ImmutableList.copyOf(items);
    }

    public static @NotNull SimpleJoin of(@NotNull String item) {
        return new SimpleJoin(ImmutableList.of(item));
    }

    public static @NotNull SimpleJoin of(@NotNull String item1, @NotNull String item2) {
        return new SimpleJoin(ImmutableList.of(item1, item2));
    }

    public static @NotNull SimpleJoin of(@NotNull String @NotNull ... items) {
        return new SimpleJoin(ImmutableList.copyOf(items));
    }

    public static @NotNull SimpleJoin of(@NotNull Object item) {
        return new SimpleJoin(ImmutableList.of(item.toString()));
    }

    public static @NotNull SimpleJoin of(@NotNull Object item1, @NotNull Object item2) {
        return new SimpleJoin(ImmutableList.of(item1.toString(), item2.toString()));
    }

    public static @NotNull SimpleJoin of(@NotNull Object @NotNull ... items) {
        return new SimpleJoin(Stream.of(items).map(Object::toString).collect(ImmutableList.toImmutableList()));
    }

    public static @NotNull SimpleJoin from(@NotNull Collection<?> pieces) {
        return new SimpleJoin(pieces.stream().map(Object::toString).collect(ImmutableList.toImmutableList()));
    }

    @CheckReturnValue
    public @NotNull SimpleJoin onlyNonEmpty() {
        return new SimpleJoin(items.stream().filter(s -> !s.isEmpty()).collect(ImmutableList.toImmutableList()));
    }

    @Pure
    @CheckReturnValue
    public @NotNull String join() {
        return String.join("", items);
    }

    @Pure
    @CheckReturnValue
    public @NotNull String join(@NotNull String separator) {
        return String.join(separator, items);
    }

    @Pure
    @Override
    public String toString() {
        return join();
    }
}
