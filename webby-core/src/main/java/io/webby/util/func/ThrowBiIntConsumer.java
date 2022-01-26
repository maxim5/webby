package io.webby.util.func;

@FunctionalInterface
public interface ThrowBiIntConsumer<U, E extends Throwable> {
    void accept(int i, U u) throws E;
}
