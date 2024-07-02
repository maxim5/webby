package io.spbx.util.func;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public class Chains {
    public static <T, R> @NotNull Function<T, R> chain(@NotNull Predicate<T> predicate, @NotNull Function<Boolean, R> func) {
        return t -> func.apply(predicate.test(t));
    }
}
