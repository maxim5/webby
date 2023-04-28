package io.webby.util.func;

import java.util.function.BiFunction;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the specialization of {@link BiFunction}, one of the arguments for which is an <code>int</code>.
 *
 * @param <T> the type of one of the arguments to the function
 * @param <R> the type of the result of the function
 *
 * @see java.util.function.Function
 * @see java.util.function.BiFunction
 * @see java.util.function.IntFunction
 */
@FunctionalInterface
public interface ObjIntBiFunction<T, R> extends BiFunction<T, Integer, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param value the second function argument
     * @return the function result
     */
    R apply(T t, int value);

    @Override
    default R apply(T t, Integer value) {
        return apply(t, value.intValue());
    }
}
