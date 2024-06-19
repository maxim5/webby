package io.spbx.util.func;

/**
 * Represents a supplier of results, potentially throwing a {@link Throwable}.
 * <p>
 * There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p>
 * The {@code ThrowSupplier} interface is similar to
 * {@link java.util.function.Supplier}, except that a {@code ThrowSupplier}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <T> the type of argument supplied
 * @param <E> the type of Throwable thrown
 * @see java.util.function.Supplier
 */
@FunctionalInterface
public interface ThrowSupplier<T, E extends Throwable> {
    /**
     * Gets a result, potentially throwing an exception.
     *
     * @return a result
     */
    T get() throws E;
}
