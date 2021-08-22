package io.webby.util.func;

@FunctionalInterface
public interface ThrowConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}
