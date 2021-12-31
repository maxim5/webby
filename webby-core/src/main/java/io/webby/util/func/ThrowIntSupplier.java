package io.webby.util.func;

@FunctionalInterface
public interface ThrowIntSupplier<E extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    int getAsInt() throws E;
}
