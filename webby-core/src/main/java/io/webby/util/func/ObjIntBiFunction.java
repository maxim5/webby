package io.webby.util.func;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ObjIntBiFunction<T, R> extends BiFunction<T, Integer, R> {
    R apply(T t, int value);

    @Override
    default R apply(T t, Integer value) {
        return apply(t, value.intValue());
    }
}
