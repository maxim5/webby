package io.spbx.util.func;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a function that accepts one argument and produces a result
 * and potentially throws a {@link Throwable}.
 * <p>
 * The {@code ThrowFunction} interface is similar to
 * {@link java.util.function.Function}, except that a {@code ThrowFunction}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of Throwable thrown
 * @see java.util.function.Function
 */
@FunctionalInterface
public interface ThrowFunction<T, R, E extends Throwable> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws E;

    /**
     * Chains the {@code consumer} of {@code R} with this function to make the consumer of {@code T}.
     */
    default @NotNull ThrowConsumer<T, E> via(@NotNull ThrowConsumer<R, E> consumer) {
        return t -> consumer.accept(apply(t));
    }
}
