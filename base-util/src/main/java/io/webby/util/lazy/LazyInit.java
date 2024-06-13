package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Allows to defer lazy value initialization. The clients should only provide a default non-null value.
 * The clients are not expected to call initialize methods with different values,
 * as only the first supplied value is remembered for the whole lifetime.
 * <p>
 * Design note: the API is tailored for the use case when the field construction and initialization can't
 * happen at the same time. The API assumes the initialization still must happen once and before any retrieval calls.
 */
public interface LazyInit<T> {
    /**
     * Returns true if this instance already holds the value.
     */
    boolean isInitialized();

    /**
     * Initializes this instance if it's not already, otherwise fails (even if it stores the same value).
     * Calling this method twice is guaranteed to throw.
     */
    void initializeOrDie(@NotNull T value);

    /**
     * Initializes this instance if it's not already, otherwise fails (unless it stores the same value).
     * Calling this method twice with different arguments is guaranteed to throw.
     */
    void initializeOrCompare(@NotNull T value);

    /**
     * Initializes this instance if it's not already, otherwise does nothing.
     * This method never throws and does not compare values.
     * The {@code valueSupplier} is only called if necessary.
     */
    @NotNull T initializeIfNotYet(@NotNull Supplier<T> valueSupplier);

    /**
     * Returns the value if this instance has been initialized, otherwise fails.
     */
    @NotNull T getOrDie();
}
