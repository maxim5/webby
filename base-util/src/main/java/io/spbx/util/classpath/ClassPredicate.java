package io.spbx.util.classpath;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

/**
 * Represents the predicate filter of the loaded class.
 * Used by the {@link ClasspathScanner} to filter out classes once they are loaded.
 * @see ClasspathScanner
 */
@FunctionalInterface
public interface ClassPredicate extends Predicate<Class<?>> {
    ClassPredicate ALLOW_ALL = item -> true;

    @Override
    boolean test(@NotNull Class<?> klass);

    @Override
    default @NotNull ClassPredicate negate() {
        return item -> !test(item);
    }

    @Override
    default @NotNull ClassPredicate and(@NotNull Predicate<? super Class<?>> other) {
        return item -> test(item) && other.test(item);
    }

    @Override
    default @NotNull ClassPredicate or(@NotNull Predicate<? super Class<?>> other) {
        return item -> test(item) || other.test(item);
    }

    static @NotNull ClassPredicate hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
        return klass -> klass.isAnnotationPresent(annotation);
    }

    static @NotNull ClassPredicate isDerived(@NotNull Class<?> superClass) {
        ClassPredicate isAssignableFrom = superClass::isAssignableFrom;  // Note: SUPERCLASS.isAssignableFrom(SUBCLASS)
        ClassPredicate isInterface = Class::isInterface;
        return isAssignableFrom.and(isInterface.negate());
    }
}
