package io.spbx.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Allows to remember a computed value. The clients should only provide a way to supply the value.
 * The clients are not expected to call {@link #getOrCompute(Supplier)} with different or non-idempotent suppliers,
 * as only the first supplied value is remembered for the whole lifetime.
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
 * @see LazyInit
 * @see Lazy
 */
public interface CacheCompute<T> {
    /**
     * Returns the lazy value, if it's there.
     * On the first call, gets the value from the {@code supplier} and remembers it.
     * Is idempotent: subsequent calls return the same lazy value.
     *
     * @param supplier provides the data on the first call
     */
    @NotNull T getOrCompute(@NotNull Supplier<T> supplier);
}
