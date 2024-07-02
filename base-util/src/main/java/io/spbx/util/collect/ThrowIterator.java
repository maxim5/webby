package io.spbx.util.collect;

import io.spbx.util.base.Unchecked;

import java.util.Iterator;

import static io.spbx.util.base.Unchecked.Runnables;
import static io.spbx.util.base.Unchecked.Suppliers;

/**
 * Same as {@link Iterator} but allows to throw check exceptions during iteration.
 * Also adapts to {@link Iterator} API but converting checked exceptions into unchecked.
 */
public interface ThrowIterator<T, E extends Throwable> extends Iterator<T> {
    boolean hasNextThrow() throws E;

    T nextThrow() throws E;

    default void removeThrow() throws E {
        throw new UnsupportedOperationException("ThrowIterator.removeThrow()");
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
