package io.webby.util.func;

@FunctionalInterface
public interface ThrowPredicate<T, E extends Throwable> {
    boolean test(T t) throws E;
}
