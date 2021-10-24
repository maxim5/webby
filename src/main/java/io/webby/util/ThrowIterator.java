package io.webby.util;

import java.util.Iterator;

import static io.webby.util.Rethrow.*;

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
            return Rethrow.rethrow(e);
        }
    }

    default T next() {
        return Suppliers.runRethrow(this::nextThrow);
    }

    default void remove() {
        Runnables.runRethrow(this::removeThrow);
    }
}
