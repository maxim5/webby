package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class ImmutableArray<E> extends BaseArray<E> {
    private ImmutableArray(@Nullable E @NotNull [] arr) {
        super(arr);
    }

    @SafeVarargs
    public static <E> @NotNull ImmutableArray<E> copyOf(@Nullable E @NotNull ... items) {
        return new ImmutableArray<>(Arrays.copyOf(items, items.length));
    }

    public static <E> @NotNull ImmutableArray<E> copyOf(@NotNull BaseArray<E> array) {
        return copyOf(array.arr);
    }

    @Override
    public E set(int index, @Nullable E element) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public void replaceAll(@NotNull UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super E> filter) {
        throw new UnsupportedOperationException("Array is immutable");
    }

    @Override
    public void sort(@NotNull Comparator<? super E> c) {
        throw new UnsupportedOperationException("Array is immutable");
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

        public @NotNull ImmutableArray<E> toArray() {
            return ImmutableArray.copyOf(arr);
        }

        public @NotNull Array<E> toMutableArray() {
            return Array.of(arr);
        }
    }
}
