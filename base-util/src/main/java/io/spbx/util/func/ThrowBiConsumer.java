package io.spbx.util.func;

/**
 * Represents an operation that accepts two input arguments and returns no result
 * potentially throws a {@link Throwable}.
 * <p>
 * The {@code ThrowBiConsumer} interface is similar to
 * {@link java.util.function.BiConsumer}, except that a {@code ThrowBiConsumer}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <T> the type of the first input to the function
 * @param <U> the type of the second input to the function
 * @param <E> the type of Throwable thrown
 * @see java.util.function.Consumer
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface ThrowBiConsumer<T, U, E extends Throwable> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void accept(T t, U u) throws E;
}
