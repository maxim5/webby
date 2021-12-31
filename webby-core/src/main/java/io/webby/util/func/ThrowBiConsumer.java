package io.webby.util.func;

@FunctionalInterface
public interface ThrowBiConsumer<T, U, E extends Throwable> {
    void accept(T t, U u) throws E;
}
