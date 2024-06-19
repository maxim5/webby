package io.spbx.util.func;

/**
 * Represents an executable piece of code, not accepting any arguments and
 * not producing any results, but potentially throwing a {@link Throwable}.
 * <p>
 * The {@code ThrowRunnable} interface is similar to
 * {@link Runnable}, except that a {@code ThrowRunnable}
 * can throw any kind of exception, including checked exceptions.
 *
 * @param <E> the type of Throwable thrown
 * @see Runnable
 */
@FunctionalInterface
public interface ThrowRunnable<E extends Throwable> {
    /**
     * Executes the potentially throwing code.
     */
    void run() throws E;
}
