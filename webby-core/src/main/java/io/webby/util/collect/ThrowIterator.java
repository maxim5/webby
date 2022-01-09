package io.webby.util.collect;

import io.webby.util.base.Unchecked;

import java.util.Iterator;

import static io.webby.util.base.Unchecked.*;

public interface ThrowIterator<T, E extends Throwable> extends Iterator<T> {
    boolean hasNextThrow() throws E;

    T nextThrow() throws E;

    default void removeThrow() throws E {
        throw new UnsupportedOperationException("remove");
    }

    default boolean hasNext() {
        try {
            return hasNextThrow();
        } catch (Throwable e) {
            return Unchecked.rethrow(e);
        }
    }

    default T next() {
        return Suppliers.runRethrow(this::nextThrow);
    }

    default void remove() {
        Runnables.runRethrow(this::removeThrow);
    }
}
