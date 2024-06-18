package io.spbx.util.func;

/**
 * Represents a function that accepts two arguments and produces a result
 * and potentially throws a {@link Throwable}.
 * This is the two-arity specialization of {@link ThrowFunction}.
 * <p>
 * The {@code ThrowBiFunction} interface is similar to
 * {@link java.util.function.BiFunction}, except that a {@code ThrowBiFunction}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <U> the type of the first argument to the function
 * @param <V> the type of the second argument to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of Throwable thrown
 */
@FunctionalInterface
public interface ThrowBiFunction<U, V, R, E extends Throwable> {
    /**
     * Applies this function to the given arguments.
     *
     * @param u the first function argument
     * @param v the second function argument
     * @return the function result
     */
    R apply(U u, V v) throws E;
}
