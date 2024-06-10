package io.webby.util.func;

/**
 * Represents a supplier of {@code long}-valued results, potentially throwing a {@link Throwable}.
 * <p>
 * There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p>
 * The {@code ThrowLongSupplier} interface is similar to
 * {@link java.util.function.LongSupplier}, except that a {@code ThrowLongSupplier}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <E> the type of Throwable thrown
 * @see java.util.function.Supplier
 * @see java.util.function.LongSupplier
 */
@FunctionalInterface
public interface ThrowLongSupplier<E extends Throwable> {
    /**
     * Gets a result, potentially throwing an exception.
     *
     * @return a result
     */
    int getAsLong() throws E;
}
