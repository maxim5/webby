package io.webby.util;

@FunctionalInterface
public interface ThrowFunction<T, R, E extends Throwable> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws E;
}
