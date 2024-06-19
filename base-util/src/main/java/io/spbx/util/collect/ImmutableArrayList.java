package io.spbx.util.collect;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import static io.spbx.util.base.EasyCast.castAny;

/**
 * An immutable version of an {@code ArrayList}.
 * <p>
 * Differences from other collections backed by an array:
 * <ul>
 *     <li>Unlike a standard {@link ArrayList}, does not allow modifications after construction.</li>
 *     <li>Unlike Guava's {@link com.google.common.collect.ImmutableList}, allows null values.</li>
 *     <li>Unlike an {@link Array} and {@link ImmutableArray},
 *     under the hood, stores the data in an Object array type ({@code Object[]}),
 *     hence supports holding items of different types.</li>
 * </ul>
 */
@Immutable
public class ImmutableArrayList<E> extends ArrayList<E> {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final ImmutableArrayList<?> EMPTY = new ImmutableArrayList<>();
    // FIX[minor]: can also optimize a single entry list
    private static final ImmutableArrayList<?> SINGLE_NULL = ListBuilder.builder(1).add(null).toImmutableArrayList();

    private static final Collector<Object, ?, ImmutableArrayList<Object>> TO_IMMUTABLE_ARRAY_LIST = Collector.of(
        ListBuilder::new,
        ListBuilder::add,
        ListBuilder::combine,
        ListBuilder::toImmutableArrayList
    );

    private ImmutableArrayList() {
        super(0);
    }

    /*package*/ ImmutableArrayList(@NotNull Collection<? extends E> c) {
        super(c.size());
        super.addAll(c);
    }

    public static <E> @NotNull ImmutableArrayList<E> of() {
        return castAny(EMPTY);
    }

    public static <E> @NotNull ImmutableArrayList<E> of(@Nullable E item) {
        return item != null ? ListBuilder.<E>builder(1).add(item).toImmutableArrayList() : castAny(SINGLE_NULL);
    }

    public static <E> @NotNull ImmutableArrayList<E> of(@Nullable E item1, @Nullable E item2) {
        return ListBuilder.<E>builder(2).add(item1).add(item2).toImmutableArrayList();
    }

    public static <E> @NotNull ImmutableArrayList<E> of(@Nullable E item1, @Nullable E item2, @Nullable E item3) {
        return ListBuilder.<E>builder(3).add(item1).add(item2).add(item3).toImmutableArrayList();
    }

    @SafeVarargs
    public static <E> @NotNull ImmutableArrayList<E> copyOf(@Nullable E @NotNull ... items) {
        // FIX[minor]: avoid extra copying - construct or create from Object[]
        return new ImmutableArrayList<>(Array.of(items));
    }

    public static <E> @NotNull ImmutableArrayList<E> copyOf(@NotNull Collection<? extends E> items) {
        return items instanceof ImmutableArrayList<?> arrayList ? castAny(arrayList) : new ImmutableArrayList<>(items);
    }

    public static <E> @NotNull ImmutableArrayList<E> copyOf(@NotNull Iterable<? extends E> items) {
        return items instanceof Collection<?> collection ?
            castAny(copyOf(collection)) :
            ListBuilder.<E>builder().addAll(items).toImmutableArrayList();
    }

    public static <E> @NotNull Collector<E, ?, ImmutableArrayList<E>> toImmutableArrayList() {
        return castAny(TO_IMMUTABLE_ARRAY_LIST);
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return new ImmutableListIterator(size());
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
        return new ImmutableListIterator(size());
    }

    @Override
    public @NotNull ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds, list size: " + size());
        }
        return new ImmutableListIterator(size(), index);
    }

    @Override
    public @NotNull ImmutableArrayList<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("Index " + fromIndex + " is out of bounds, list size: " + size());
        }
        if (toIndex > size()) {
            throw new IndexOutOfBoundsException("Index " + toIndex + " is out of bounds, list size: " + size());
        }
        if (fromIndex == toIndex) {
            return castAny(EMPTY);
        }
        /* Arrays.copyOfRange(this.elementData, fromIndex, toIndex) */
        return new ImmutableArrayList<>(super.subList(fromIndex, toIndex));
    }

    @Override
    public E set(int index, @Nullable E element) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public void replaceAll(@NotNull UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public void trimToSize() {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super E> filter) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    @Override
    public void sort(@NotNull Comparator<? super E> c) {
        throw new UnsupportedOperationException("ArrayList is immutable");
    }

    private class ImmutableListIterator implements ListIterator<E> {
        private int cursor;
        private final int size;

        ImmutableListIterator(int size, int position) {
            this.size = size;
            this.cursor = position;
        }

        ImmutableListIterator(int size) {
            this(size, 0);
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return get(cursor++);
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return get(--cursor);
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("ArrayList is immutable");
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException("ArrayList is immutable");
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException("ArrayList is immutable");
        }
    }
}
