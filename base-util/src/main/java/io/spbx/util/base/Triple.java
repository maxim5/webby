package io.spbx.util.base;

import com.google.errorprone.annotations.Immutable;
import io.spbx.util.func.TriConsumer;
import io.spbx.util.func.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Holds an immutable triple of nullable objects.
 *
 * @see Pair
 * @see OneOf
 */
@Immutable
public record Triple<U, V, W>(U first, V second, W third) {
    public static <U, V, W> @NotNull Triple<U, V, W> of(U first, V second, W third) {
        return new Triple<>(first, second, third);
    }

    public static <T> @NotNull Triple<T, T, T> of(@NotNull T[] array) {
        assert array.length == 3 : "Invalid array to create a pair from: length=%d".formatted(array.length);
        return new Triple<>(array[0], array[1], array[2]);
    }

    public <T, S, R> @NotNull Triple<T, S, R> map(@NotNull Function<U, T> convertFirst,
                                                  @NotNull Function<V, S> convertSecond,
                                                  @NotNull Function<W, R> convertThird) {
        return of(convertFirst.apply(first), convertSecond.apply(second), convertThird.apply(third));
    }

    public <T> @NotNull Triple<T, V, W> mapFirst(@NotNull Function<U, T> convert) {
        return map(convert, second -> second, third -> third);
    }

    public <T> @NotNull Triple<U, T, W> mapSecond(@NotNull Function<V, T> convert) {
        return map(first -> first, convert, third -> third);
    }

    public <T> @NotNull Triple<U, V, T> mapThird(@NotNull Function<W, T> convert) {
        return map(first -> first, second -> second, convert);
    }

    public <T> @NotNull T mapToObj(@NotNull TriFunction<U, V, W, T> convert) {
        return convert.apply(first, second, third);
    }

    public boolean testFirst(@NotNull Predicate<U> predicate) {
        return predicate.test(first);
    }

    public boolean testSecond(@NotNull Predicate<V> predicate) {
        return predicate.test(second);
    }

    public boolean testThird(@NotNull Predicate<W> predicate) {
        return predicate.test(third);
    }

    public void apply(@NotNull TriConsumer<U, V, W> action) {
        action.accept(first, second, third);
    }

    @Override
    public String toString() {
        return "(%s, %s, %s)".formatted(first, second, third);
    }
}
