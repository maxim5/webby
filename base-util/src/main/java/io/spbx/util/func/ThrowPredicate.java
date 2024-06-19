package io.spbx.util.func;

/**
 * Represents a predicate (boolean-valued function) of one argument,
 * potentially throwing a {@link Throwable}.
 * <p>
 * The {@code ThrowPredicate} interface is similar to
 * {@link java.util.function.Predicate}, except that a {@code ThrowPredicate}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <T> the type of the input to the predicate
 * @param <E> the type of Throwable thrown
 * @see java.util.function.Predicate
 */
@FunctionalInterface
public interface ThrowPredicate<T, E extends Throwable> {
    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    boolean test(T t) throws E;
}
