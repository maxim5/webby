package io.webby.util;

import java.util.Map;

public record Pair<T, U>(T first, U second) implements Map.Entry<T, U> {
    public static <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<>(first, second);
    }

    public static <T, U> Pair<T, U> of(Map.Entry<T, U> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    public static <T> Pair<T, T> of(T[] array) {
        assert array.length == 2 : "Invalid array to create a pair from: %d".formatted(array.length);
        return new Pair<>(array[0], array[1]);
    }

    public Pair<U, T> swap() {
        return of(second, first);
    }

    @Override
    public T getKey() {
        return first;
    }

    @Override
    public U getValue() {
        return second;
    }

    @Override
    public U setValue(U value) {
        throw new UnsupportedOperationException("Pair is immutable");
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
