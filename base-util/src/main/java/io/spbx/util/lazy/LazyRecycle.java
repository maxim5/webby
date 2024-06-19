package io.spbx.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A {@link LazyInit} that in addition allows to {@link #recycle} the state to non-initialized.
 */
public interface LazyRecycle<T> extends LazyInit<T> {
    /**
     * Resets the lazy state.
     * After the call {@link #isInitialized()} returns <code>false</code>.
     * Does nothing if not initialized.
     */
    void recycle();

    /**
     * Resets the lazy state and calls the <code>terminator</code> on the current instance if it's initialized.
     * After the call {@link #isInitialized()} returns <code>false</code>.
     * Does nothing if not initialized.
     */
    default void recycle(@NotNull Consumer<T> terminator) {
        if (isInitialized()) {
            terminator.accept(getOrDie());
            recycle();
        }
    }
}
