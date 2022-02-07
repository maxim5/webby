package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class Array<E> extends AbstractList<E> {
    private final E[] arr;

    private Array(@NotNull E[] arr) {
        this.arr = arr;
    }

    @SafeVarargs
    public static <E> @NotNull Array<E> of(@Nullable E @NotNull ... items) {
        //noinspection NullableProblems
        return new Array<>(items);
    }

    @Override
    public int size() {
        return arr.length;
    }

    @Override
    public E get(int index) {
        assert index >= 0 && index < arr.length: "Index out of bounds: " + index;
        return arr[index];
    }

    @Override
    public E set(int index, @Nullable E element) {
        assert index >= 0 && index < arr.length : "Index out of bounds: " + index;
        E oldValue = arr[index];
        arr[index] = element;
        return oldValue;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        E[] arr = this.arr;
        if (o == null) {
            for (int i = 0; i < arr.length; i++)
                if (arr[i] == null)
                    return i;
        } else {
            for (int i = 0; i < arr.length; i++)
                if (o.equals(arr[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(arr, Spliterator.ORDERED);
    }

    @Override
    public void forEach(@NotNull Consumer<? super E> action) {
        for (E e : arr) {
            action.accept(e);
        }
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

    public @NotNull Builder<E> toBuilder() {
        return Builder.of(this);
    }

    public static <E> @NotNull Builder<E> builder() {
        return Builder.of();
    }

    public static class Builder<E> {
        private E[] arr;

        private Builder(@NotNull E[] arr) {
            this.arr = arr;
        }

        @SafeVarargs
        public static <E> @NotNull Builder<E> of(@NotNull E @Nullable ... items) {
            return new Builder<>(items);
        }

        public static <E> @NotNull Builder<E> of(@NotNull Array<E> array) {
            return new Builder<>(array.arr);
        }

        public @NotNull Builder<E> add(@Nullable E item) {
            grow(1);
            arr[arr.length - 1] = item;
            return this;
        }

        public @NotNull Builder<E> addAll(@NotNull Collection<? extends E> items) {
            int length = this.arr.length - 1;
            E[] arr = grow(items.size());
            for (E item : items) {
                arr[++length] = item;
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        public @NotNull Builder<E> addAll(@Nullable E @NotNull ... items) {
            int length = this.arr.length - 1;
            E[] arr = grow(items.length);
            for (E item : items) {
                arr[++length] = item;
            }
            return this;
        }

        public @NotNull Array<E> toArray() {
            return new Array<>(arr);
        }

        private @NotNull E[] grow(int extra) {
            return arr = Arrays.copyOf(arr, arr.length + extra);
        }
    }
}
