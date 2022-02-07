package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.UnaryOperator;

public final class Array<E> extends BaseArray<E> {
    private Array(@Nullable E @NotNull[] arr) {
        super(arr);
    }

    @SafeVarargs
    public static <E> @NotNull Array<E> of(@Nullable E @NotNull ... items) {
        return new Array<>(items);
    }

    @SafeVarargs
    public static <E> @NotNull Array<E> copyOf(@Nullable E @NotNull ... items) {
        return new Array<>(Arrays.copyOf(items, items.length));
    }

    public static <E> @NotNull Array<E> copyOf(@NotNull BaseArray<E> array) {
        return copyOf(array.arr);
    }

    @Override
    public E set(int index, @Nullable E element) {
        assert index >= 0 && index < arr.length : "Index out of bounds: " + index;
        E oldValue = arr[index];
        arr[index] = element;
        return oldValue;
    }

    @Override
    public void replaceAll(@NotNull UnaryOperator<E> operator) {
        E[] arr = this.arr;
        for (int i = 0; i < arr.length; i++) {
            arr[i] = operator.apply(arr[i]);
        }
    }

    @Override
    public void sort(@NotNull Comparator<? super E> c) {
        Arrays.sort(arr, c);
    }

    public static <E> @NotNull Builder<E> builder() {
        return Builder.of();
    }

    public @NotNull Builder<E> toBuilder() {
        return Builder.of(this);
    }

    public static final class Builder<E> extends BaseBuilder<E> {
        private Builder(@NotNull E[] arr) {
            super(arr);
        }

        @SafeVarargs
        public static <E> @NotNull Builder<E> of(@NotNull E @Nullable ... items) {
            return new Builder<>(items);
        }

        public static <E> @NotNull Builder<E> of(@NotNull BaseArray<E> array) {
            return new Builder<>(array.arr);
        }

        @Override
        public @NotNull Builder<E> add(@Nullable E item) {
            super.add(item);
            return this;
        }

        @Override
        public @NotNull Builder<E> addAll(@NotNull Collection<? extends E> items) {
            super.addAll(items);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Builder<E> addAll(@Nullable E @NotNull ... items) {
            super.addAll(items);
            return this;
        }

        public @NotNull Array<E> toArray() {
            return Array.of(arr);
        }

        public @NotNull ImmutableArray<E> toImmutableArray() {
            return ImmutableArray.copyOf(arr);
        }
    }
}
