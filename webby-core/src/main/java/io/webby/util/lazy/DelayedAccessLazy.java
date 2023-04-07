package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Allows to store a lazy value. The clients should only provide a way to supply the way.
 * The clients are not expected to call {@link #lazyGet(Supplier)} with different or non-idempotent suppliers,
 * as only the first supplied value is remembered for the whole lifetime.
 * <p>
 * Design note: by asking for a supplier at a call time, the API ensures the client doesn't have to deal with
 * an uninitialized state. This is most convenient for the caching use case.
 */
public interface DelayedAccessLazy<T> {
    /**
     * Returns the lazy value, if it's there.
     * On the first call, gets the value from the {@code supplier} and remembers it.
     * Is idempotent: subsequent calls return the same lazy value.
     *
     * @param supplier provides the data on the first call
     */
    @NotNull T lazyGet(@NotNull Supplier<T> supplier);
}
