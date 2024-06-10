package io.webby.common;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import io.webby.app.ClassFilter;
import io.webby.util.base.Unchecked;
import io.webby.util.time.TimeIt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ClasspathScanner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    // Currently, static to speed up tests. Guice injection is single-threaded.
    // Can consider dynamic class loading when necessary.
    private static final AtomicReference<ClassPath> classPathRef = new AtomicReference<>(null);

    @Inject
    public ClasspathScanner(@NotNull EventBus eventBus) {
        eventBus.register(this);
    }

    @VisibleForTesting
    public ClasspathScanner() {
    }

    @Subscribe
    public void guiceComplete(@NotNull GuiceCompleteEvent event) {
        log.at(Level.FINE).log("Optimizing ClasspathScanner memory footprint");
        classPathRef.set(null);
    }

    public @NotNull Set<? extends Class<?>> getAnnotatedClasses(@NotNull ClassFilter classInfoFilter,
                                                                @NotNull Class<? extends Annotation> annotation) {
        return getMatchingClasses(classInfoFilter, hasAnnotation(annotation), annotation.getSimpleName());
    }

    public @NotNull Set<? extends Class<?>> getDerivedClasses(@NotNull ClassFilter classInfoFilter,
                                                              @NotNull Class<?> superClass) {
        return getMatchingClasses(classInfoFilter, isDerived(superClass), superClass.getSimpleName());
    }

    public static @NotNull Predicate<Class<?>> hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
        return klass -> klass.isAnnotationPresent(annotation);
    }

    public static @NotNull Predicate<Class<?>> isDerived(@NotNull Class<?> superClass) {
        Predicate<Class<?>> isAssignableFrom = superClass::isAssignableFrom;  // Note: SUPERCLASS.isAssignableFrom(SUBCLASS)
        Predicate<Class<?>> isInterface = Class::isInterface;
        return isAssignableFrom.and(isInterface.negate());
    }

    public @NotNull Set<? extends Class<?>> getMatchingClasses(@NotNull ClassFilter classInfoFilter,
                                                               @NotNull Predicate<Class<?>> classFilter,
                                                               @NotNull String description) {
        assert classInfoFilter.isSet() : "Class info filter is unset";
        return TimeIt.timeIt(
            () -> scan(classInfoFilter.predicateOrDefault(), classFilter),
            (result, millis) -> log.at(Level.INFO).log("Found %d %s classes in %d ms", result.size(), description, millis)
        );
    }

    private @NotNull ClassPath classPath() {
        return classPathRef.updateAndGet(current -> {
            try {
                return current != null ? current : ClassPath.from(ClassLoader.getSystemClassLoader());
            } catch (Exception e) {
                return Unchecked.rethrow(e);
            }
        });
    }

    private @NotNull Set<? extends Class<?>> scan(@NotNull BiPredicate<String, String> classInfoFilter,
                                                  @NotNull Predicate<Class<?>> classFilter) {
        return classPath()
                .getAllClasses()
                .stream()
                .filter(classInfo -> classInfoFilter.test(classInfo.getPackageName(), classInfo.getSimpleName()))
                .map((ClassPath.ClassInfo classInfo) -> {
                    try {
                        return classInfo.load();
                    } catch (NoClassDefFoundError e) {
                        log.at(Level.WARNING).withCause(e)
                                .log("Failed to load class: %s.%s", classInfo.getPackageName(), classInfo.getName());
                        return null;
                    }
                })
                .filter(klass -> klass != null && classFilter.test(klass))
                .collect(Collectors.toSet());
    }
}
