package io.spbx.util.classpath;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Allows to iterate over the classpath to filter out classes matching specified criteria,
 * for example all classes implementing a particular interface or all classes annotated by the
 * particular annotation.
 */
public interface ClasspathScanner {
    /**
     * Scans the classpath and passes classes matching {@code classNamePredicate} and {@code classPredicate}
     * predicates to the specified {@code consumer}.
     */
    void scan(@NotNull ClassNamePredicate classNamePredicate,
              @NotNull ClassPredicate classPredicate,
              @NotNull Consumer<Class<?>> consumer);

    /**
     * Scans the classpath and passes classes matching {@code ClassNamePredicate} predicate
     * to the specified {@code consumer}.
     */
    default void scan(@NotNull ClassNamePredicate classNamePredicate, @NotNull Consumer<Class<?>> consumer) {
        scan(classNamePredicate, ClassPredicate.ALLOW_ALL, consumer);
    }

    /**
     * Scans the classpath and passes classes matching {@code classPredicate} predicate
     * to the specified {@code consumer}.
     */
    default void scan(@NotNull ClassPredicate classPredicate, @NotNull Consumer<Class<?>> consumer) {
        scan(ClassNamePredicate.ALLOW_ALL, classPredicate, consumer);
    }

    /**
     * Scans the classpath and returns classes matching {@code classNamePredicate} and {@code classPredicate} predicates.
     */
    @NotNull Set<Class<?>> scanToSet(@NotNull ClassNamePredicate classNamePredicate, @NotNull ClassPredicate classPredicate);

    /**
     * Scans the classpath and returns classes matching {@code classNamePredicate} predicate.
     */
    default @NotNull Set<Class<?>> scanToSet(@NotNull ClassNamePredicate classNamePredicate) {
        return scanToSet(classNamePredicate, ClassPredicate.ALLOW_ALL);
    }

    /**
     * Scans the classpath and returns classes matching {@code classPredicate} predicate.
     */
    default @NotNull Set<Class<?>> scanToSet(@NotNull ClassPredicate classPredicate) {
        return scanToSet(ClassNamePredicate.ALLOW_ALL, classPredicate);
    }

    /**
     * Converts this scanner to an unnamed {@link TimedClasspathScanner}.
     */
    default @NotNull ClasspathScanner timed() {
        return timed("");
    }

    /**
     * Converts this scanner to a named {@link TimedClasspathScanner}.
     */
    default @NotNull ClasspathScanner timed(@NotNull String name) {
        return new TimedClasspathScanner(this, name);
    }

    /**
     * Scans the classpath and passes classes matching {@code classNamePredicate} and annotated with {@code annotation}
     * to the specified {@code consumer}.
     */
    default void scanAnnotatedClasses(@NotNull ClassNamePredicate classNamePredicate,
                                      @NotNull Class<? extends Annotation> annotation,
                                      @NotNull Consumer<Class<?>> consumer) {
        scan(classNamePredicate, ClassPredicate.hasAnnotation(annotation), consumer);
    }

    /**
     * Scans the classpath and returns classes matching {@code classNamePredicate} and annotated with {@code annotation}.
     */
    default @NotNull Set<Class<?>> getAnnotatedClasses(@NotNull ClassNamePredicate classNamePredicate,
                                                       @NotNull Class<? extends Annotation> annotation) {
        return scanToSet(classNamePredicate, ClassPredicate.hasAnnotation(annotation));
    }

    /**
     * Scans the classpath and passes classes matching {@code classNamePredicate} and derived from {@code superClass}
     * to the specified {@code consumer}.
     */
    default void scanDerivedClasses(@NotNull ClassNamePredicate classNamePredicate,
                                    @NotNull Class<?> superClass,
                                    @NotNull Consumer<Class<?>> consumer) {
        scan(classNamePredicate, ClassPredicate.isDerived(superClass), consumer);
    }

    /**
     * Scans the classpath and returns classes matching {@code classNamePredicate} and derived from {@code superClass}.
     */
    default @NotNull Set<Class<?>> getDerivedClasses(@NotNull ClassNamePredicate classNamePredicate,
                                                     @NotNull Class<?> superClass) {
        return scanToSet(classNamePredicate, ClassPredicate.isDerived(superClass));
    }
}
