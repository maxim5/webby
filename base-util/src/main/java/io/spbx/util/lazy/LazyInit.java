package io.spbx.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Allows to defer lazy value initialization. The clients should only provide a default non-null value.
 * The clients are not expected to call initialize methods with different values,
 * as only the first supplied value is remembered for the whole lifetime.
 * <p>
 * Use this util when the field construction and initialization can't happen at the same time, but
 * the init must happen exactly once and before any retrieval calls.
 * <p>
 * Designed use-cases and comparison to other caching utils:
 * <ul>
 *     <li>
 *         {@link CacheCompute} is designed as a caching wrapper of the parameter-less method implementation.
 *         The value provider is known and can be specified at the init time,
 *         but logically it belongs to the call-site rather than construction.
 *         Hence, {@link CacheCompute#getOrCompute} usually has just one call-site.
 *         The caching class implementation ensures the supplier is only going to be called once, and
 *         the client is not responsible for the taking care of the uninitialized state.
 *     </li>
 *     <li>
 *         {@link Lazy} ({@link LazyBoolean}, etc) is designed as a deferring wrapper of the field init.
 *         The value provider is known at the init-time, but has to be deferred.
 *         Access methods like {@link Lazy#get()} can be used in multiple call-sites.
 *         The lazy class implementation ensures the supplier is only going to be called once, and
 *         the client is not responsible for the taking care of the uninitialized state.
 *     </li>
 *     <li>
 *         {@link LazyInit} is designed to defer field init from an external input.
 *         The value provider can not be set at the init-time, but the value can be accessed via
 *         {@link LazyInit#getOrDie()} in multiple call-sites.
 *         Hence, it has dedicated methods like {@link LazyInit#initializeOrDie} instead of the supplier,
 *         and the client is responsible for calling those methods correctly and dealing with uninitialized state.
 *     </li>
 * </ul>
 *
 * @see CacheCompute
 * @see Lazy
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
    @NotNull
    T initializeIfNotYet(@NotNull Supplier<T> valueSupplier);

    /**
     * Returns the value if this instance has been initialized, otherwise fails.
     */
    @NotNull
    T getOrDie();
}
