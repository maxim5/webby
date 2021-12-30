package io.webby.util.func;

@FunctionalInterface
public interface ThrowSupplier<T, E extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws E;
}
