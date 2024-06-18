package io.webby.util.base;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.*;

/**
 * Can hold an immutable pair of nullable objects.
 */
@Immutable
public record Pair<U, V>(U first, V second) implements Map.Entry<U, V> {
    public static <U, V> @NotNull Pair<U, V> of(U first, V second) {
        return new Pair<>(first, second);
    }

    public static <U, V> @NotNull Pair<U, V> of(@NotNull Map.Entry<U, V> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    public static <T> @NotNull Pair<T, T> of(@NotNull T[] array) {
        assert array.length == 2 : "Invalid array to create a pair from: length=%d".formatted(array.length);
        return new Pair<>(array[0], array[1]);
    }

    public @NotNull Pair<V, U> swap() {
        return of(second, first);
    }

    public <T, S> @NotNull Pair<T, S> map(@NotNull Function<U, T> convertFirst, @NotNull Function<V, S> convertSecond) {
        return of(convertFirst.apply(first), convertSecond.apply(second));
    }

    public <T> @NotNull Pair<T, V> mapFirst(@NotNull Function<U, T> convert) {
        return map(convert, second -> second);
    }

    public <T> @NotNull Pair<U, T> mapSecond(@NotNull Function<V, T> convert) {
        return map(first -> first, convert);
    }

    public <T> @NotNull T mapToObj(@NotNull BiFunction<U, V, T> convert) {
        return convert.apply(first, second);
    }

    public int mapToInt(@NotNull ToIntBiFunction<U, V> convert) {
        return convert.applyAsInt(first, second);
    }

    public long mapToLong(@NotNull ToLongBiFunction<U, V> convert) {
        return convert.applyAsLong(first, second);
    }

    public double mapToDouble(@NotNull ToDoubleBiFunction<U, V> convert) {
        return convert.applyAsDouble(first, second);
    }

    public boolean testFirst(@NotNull Predicate<U> predicate) {
        return predicate.test(first);
    }

    public boolean testSecond(@NotNull Predicate<V> predicate) {
        return predicate.test(second);
    }

    public boolean test(@NotNull BiPredicate<U, V> predicate) {
        return predicate.test(first, second);
    }

    public void apply(@NotNull BiConsumer<U, V> action) {
        action.accept(first, second);
    }

    @Override
    public U getKey() {
        return first;
    }

    @Override
    public V getValue() {
        return second;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("Pair is immutable");
    }

    @Override
    public String toString() {
        return "(%s, %s)".formatted(first, second);
    }
}
