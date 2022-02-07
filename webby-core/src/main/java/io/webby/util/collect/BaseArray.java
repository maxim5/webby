package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public abstract class BaseArray<E> extends AbstractList<E> {
    protected final E[] arr;

    protected BaseArray(@Nullable E @NotNull [] arr) {
        this.arr = arr;
    }

    @Override
    public int size() {
        return arr.length;
    }

    @Override
    public E get(int index) {
        assert index >= 0 && index < arr.length : "Index out of bounds: " + index;
        return arr[index];
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

    public abstract @NotNull BaseBuilder<E> toBuilder();

    public abstract static class BaseBuilder<E> {
        protected E[] arr;

        protected BaseBuilder(@NotNull E[] arr) {
            this.arr = arr;
        }

        public @NotNull BaseBuilder<E> add(@Nullable E item) {
            grow(1);
            arr[arr.length - 1] = item;
            return this;
        }

        public @NotNull BaseBuilder<E> addAll(@NotNull Collection<? extends E> items) {
            int length = this.arr.length - 1;
            E[] arr = grow(items.size());
            for (E item : items) {
                arr[++length] = item;
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        public @NotNull BaseBuilder<E> addAll(@Nullable E @NotNull ... items) {
            int length = this.arr.length - 1;
            E[] arr = grow(items.length);
            for (E item : items) {
                arr[++length] = item;
            }
            return this;
        }

        private @NotNull E[] grow(int extra) {
            return arr = Arrays.copyOf(arr, arr.length + extra);
        }
    }
}
