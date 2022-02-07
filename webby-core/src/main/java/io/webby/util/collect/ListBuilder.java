package io.webby.util.collect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@CanIgnoreReturnValue
public class ListBuilder<T> {
    private final List<T> list;

    public ListBuilder() {
        list = new ArrayList<>();
    }

    public ListBuilder(int size) {
        list = new ArrayList<>(size);
    }

    public static <T> @NotNull ListBuilder<T> builder() {
        return new ListBuilder<>();
    }

    public static <T> @NotNull ListBuilder<T> builder(int size) {
        return new ListBuilder<>(size);
    }

    public @NotNull ListBuilder<T> add(@Nullable T item) {
        list.add(item);
        return this;
    }

    public @NotNull ListBuilder<T> addAll(@NotNull Iterable<? extends T> items) {
        for (T item : items) {
            list.add(item);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public @NotNull ListBuilder<T> addAll(@Nullable T @NotNull ... items) {
        return addAll(Arrays.asList(items));
    }

    public @NotNull ListBuilder<T> addAll(@Nullable T item) {
        return add(item);
    }

    public @NotNull ListBuilder<T> addAll(@Nullable T item1, @Nullable T item2) {
        list.add(item1);
        list.add(item2);
        return this;
    }

    /*package*/ @NotNull ListBuilder<T> combine(@NotNull ListBuilder<T> builder) {
        addAll(builder.list);
        return this;
    }

    // does not allow nulls
    public @NotNull List<T> toList() {
        return List.copyOf(list);
    }

    // allows nulls
    public @NotNull ArrayList<T> toArrayList() {
        return new ArrayList<>(list);
    }

    // does not allow nulls
    public @NotNull ImmutableList<T> toImmutableList() {
        return ImmutableList.copyOf(list);
    }

    // allows nulls
    public @NotNull ImmutableArrayList<T> toImmutableArrayList() {
        return new ImmutableArrayList<>(list);
    }

    // allows nulls
    public @NotNull Array<T> toArray() {
        return Array.<T>builder().addAll(list).toArray();
    }

    // allows nulls
    public @NotNull ImmutableArray<T> toImmutableArray() {
        return ImmutableArray.<T>builder().addAll(list).toArray();
    }

    public static <E> @NotNull List<E> concat(@NotNull Iterable<E> first, @NotNull Iterable<E> second) {
        return Stream.concat(Streams.stream(first), Streams.stream(second)).toList();
    }

    public static <E> @NotNull List<E> concat(@NotNull List<E> first, @NotNull List<E> second) {
        ArrayList<E> result = new ArrayList<>(first.size() + second.size());
        result.addAll(first);
        result.addAll(second);
        return result;
    }

    public static <E> @NotNull List<E> concatOne(@NotNull Iterable<E> first, @NotNull E second) {
        return Stream.concat(Streams.stream(first), Stream.of(second)).toList();
    }
}
