package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

/**
 * Allows to store a lazy value. The clients should only provide a default non-null value.
 * The clients are not expected to call initialize methods with different values,
 * as only the first supplied value is remembered for the whole lifetime.
 * <p>
 * Design note: the API is tailored for the use case when the field construction and initialization can't
 * happen at the same time. The API assumes the initialization still must happen once and before any retrieval calls.
 */
public interface DelayedInitLazy<T> {
    /**
     * Returns true if the instance already holds the value.
     */
    boolean isInitialized();

    /**
     * Initializes the instance if it's not already, otherwise fails (even if it stores the same value).
     * Calling this method twice is guaranteed to throw.
     */
    void initializeOrDie(@NotNull T value);

    /**
     * Initializes the instance if it's not already, otherwise fails (unless it stores the same value).
     * Calling this method twice with different arguments is guaranteed to throw.
     */
    void initializeOrCompare(@NotNull T value);

    /**
     * Returns the value if the instance has been initialized, otherwise fails.
     */
    @NotNull T getOrDie();
}
