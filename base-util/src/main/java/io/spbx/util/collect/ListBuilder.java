package io.spbx.util.collect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.util.func.NullAwareFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A universal {@link List} builder. Supports null values.
 */
@CanIgnoreReturnValue
public class ListBuilder<T> {
    private final ArrayList<T> list;

    private ListBuilder(@NotNull ArrayList<T> list) {
        this.list = list;
    }

    public ListBuilder() {
        this(new ArrayList<>());
    }

    public ListBuilder(int size) {
        this(new ArrayList<>(size));
    }

    public static <T> @NotNull ListBuilder<T> builder() {
        return new ListBuilder<>();
    }

    public static <T> @NotNull ListBuilder<T> builder(int size) {
        return new ListBuilder<>(size);
    }

    @SafeVarargs
    public static <T> @NotNull ListBuilder<T> of(@Nullable T @NotNull ... items) {
        return ListBuilder.<T>builder(items.length).addAll(items);
    }

    public static <T> @NotNull ListBuilder<T> of(@NotNull Iterable<? extends T> items) {
        return ListBuilder.<T>builder(items instanceof Collection<?> collection ? collection.size() : 8).addAll(items);
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

    public @NotNull ListBuilder<T> addAll(@NotNull Collection<? extends T> items) {
        list.addAll(items);
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

    public <U> @NotNull ListBuilder<U> map(@NotNull Function<T, U> func) {
        ArrayList<U> arrayList = list.stream().map(func).collect(Collectors.toCollection(ArrayList::new));
        return new ListBuilder<>(arrayList);
    }

    public <U> @NotNull ListBuilder<U> mapSafe(@NotNull Function<T, U> func) {
        return map(NullAwareFunction.wrap(func));
    }

    public @NotNull ListBuilder<T> excludeIf(@NotNull Predicate<T> predicate) {
        list.removeIf(predicate);
        return this;
    }

    public @NotNull ListBuilder<T> excludeIfSafe(@NotNull Predicate<T> predicate) {
        list.removeIf(t -> t != null && predicate.test(t));
        return this;
    }

    public @NotNull ListBuilder<T> withoutNulls() {
        return excludeIf(Objects::isNull);
    }

    /*package*/ @NotNull ListBuilder<T> combine(@NotNull ListBuilder<T> builder) {
        addAll(builder.list);
        return this;
    }

    // does not allow nulls
    public @NotNull List<T> toList() {
        assert !containsNulls() : "The builder contains nulls: toList() is not supported: " + list;
        return List.copyOf(list);
    }

    // allows nulls
    public @NotNull ArrayList<T> toArrayList() {
        return new ArrayList<>(list);
    }

    // does not allow nulls
    public @NotNull ImmutableList<T> toGuavaImmutableList() {
        assert !containsNulls() : "The builder contains nulls: toGuavaImmutableList() is not supported: " + list;
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

    // allows nulls
    public @Nullable T @NotNull [] toNativeArray(@NotNull IntFunction<T[]> generator) {
        return list.toArray(generator);
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

    public static <E> @NotNull List<E> concatOne(@NotNull Iterable<E> first, @Nullable E second) {
        return Stream.concat(Streams.stream(first), Stream.of(second)).toList();
    }

    @VisibleForTesting
    /*package*/ boolean containsNulls() {
        return list.stream().anyMatch(Objects::isNull);
    }
}
