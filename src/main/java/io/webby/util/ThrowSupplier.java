package io.webby.util;

@FunctionalInterface
public interface ThrowSupplier<T, E extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws E;
}
