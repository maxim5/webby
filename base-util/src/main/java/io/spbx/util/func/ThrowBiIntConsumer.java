package io.spbx.util.func;

/**
 * Represents an operation that accepts two input arguments one of which is an <code>int</code>
 * and returns no result potentially throws a {@link Throwable}.
 * <p>
 * The {@code ThrowBiConsumer} interface is similar to
 * {@link java.util.function.BiConsumer}, except that a {@code ThrowBiConsumer}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <U> the type of one of the inputs to the function
 * @param <E> the type of Throwable thrown
 * @see java.util.function.Consumer
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface ThrowBiIntConsumer<U, E extends Throwable> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param i the first input argument
     * @param u the second input argument
     */
    void accept(int i, U u) throws E;
}
