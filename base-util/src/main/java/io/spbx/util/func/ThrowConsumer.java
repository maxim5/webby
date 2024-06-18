package io.spbx.util.func;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that can be used to implement any generic block of code that consumes an argument and
 * potentially throws a {@link Throwable}.
 * <p>
 * The {@code ThrowingConsumer} interface is similar to
 * {@link java.util.function.Consumer}, except that a {@code ThrowingConsumer}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <T> the type of the input to the function
 * @param <E> the type of Throwable thrown
 */
@FunctionalInterface
public interface ThrowConsumer<T, E extends Throwable> {
    /**
     * Consume the supplied argument, potentially throwing an exception.
     *
     * @param t the argument to consume
     */
    void accept(T t) throws E;

    /**
     * Returns a composed {@code ThrowConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ThrowConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default @NotNull ThrowConsumer<T, E> andThen(@NotNull ThrowConsumer<? super T, E> after) {
        return (T t) -> { accept(t); after.accept(t); };
    }
}
