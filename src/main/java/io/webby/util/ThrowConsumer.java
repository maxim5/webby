package io.webby.util;

@FunctionalInterface
public interface ThrowConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}
