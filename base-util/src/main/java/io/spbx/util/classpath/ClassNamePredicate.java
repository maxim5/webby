package io.spbx.util.classpath;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.function.BiPredicate;

/**
 * Represents the predicate filter of the class info: class name and package name.
 * Used by the {@link ClasspathScanner} to filter out classes before loading them.
 * @see ClasspathScanner
 */
@FunctionalInterface
public interface ClassNamePredicate extends BiPredicate<String, String>, Serializable {
    ClassNamePredicate ALLOW_ALL = (packageName, simpleClassName) -> true;

    @Override
    boolean test(@NotNull String packageName, @NotNull String simpleClassName);

    @Override
    default @NotNull ClassNamePredicate negate() {
        return (packageName, simpleClassName) -> !test(packageName, simpleClassName);
    }

    @Override
    default @NotNull ClassNamePredicate and(@NotNull BiPredicate<? super String, ? super String> other) {
        return (packageName, simpleClassName) -> test(packageName, simpleClassName) &&
                                                 other.test(packageName, simpleClassName);
    }

    @Override
    default @NotNull ClassNamePredicate or(@NotNull BiPredicate<? super String, ? super String> other) {
        return (packageName, simpleClassName) -> test(packageName, simpleClassName) ||
                                                 other.test(packageName, simpleClassName);
    }

    default @NotNull ClassNamePredicate xor(@NotNull BiPredicate<? super String, ? super String> other) {
        return (packageName, simpleClassName) -> test(packageName, simpleClassName) ^
                                                 other.test(packageName, simpleClassName);
    }
}
