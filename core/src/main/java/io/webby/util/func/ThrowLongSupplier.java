package io.webby.util.func;

@FunctionalInterface
public interface ThrowLongSupplier<E extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    int getAsInt() throws E;
}
