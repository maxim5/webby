package io.webby.common;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.common.reflect.ClassPath;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static io.webby.util.Rethrow.Suppliers.rethrow;

@SuppressWarnings("UnstableApiUsage")
public class ClasspathScanner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final ClassPath classPath = rethrow(() -> ClassPath.from(ClassLoader.getSystemClassLoader())).get();

    @NotNull
    public Set<? extends Class<?>> getAnnotatedClasses(@NotNull BiPredicate<String, String> classInfoFilter,
                                                       @NotNull Class<? extends Annotation> annotation) {
        return getMatchingClasses(classInfoFilter, hasAnnotation(annotation), annotation.getSimpleName());
    }

    @NotNull
    public Set<? extends Class<?>> getDerivedClasses(@NotNull BiPredicate<String, String> classInfoFilter,
                                                     @NotNull Class<?> superClass) {
        return getMatchingClasses(classInfoFilter, isDerived(superClass), superClass.getSimpleName());
    }

    @NotNull
    public static Predicate<Class<?>> hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
        return klass -> klass.isAnnotationPresent(annotation);
    }

    @NotNull
    public static Predicate<Class<?>> isDerived(@NotNull Class<?> superClass) {
        Predicate<Class<?>> isAssignableFrom = superClass::isAssignableFrom;  // Note: SUPERCLASS.isAssignableFrom(SUBCLASS)
        Predicate<Class<?>> isInterface = Class::isInterface;
        return isAssignableFrom.and(isInterface.negate());
    }

    @NotNull
    public Set<? extends Class<?>> getMatchingClasses(@NotNull BiPredicate<String, String> classInfoFilter,
                                                      @NotNull Predicate<Class<?>> classFilter,
                                                      @NotNull String description) {
        return timeIt(
            () -> scan(classInfoFilter, classFilter),
            (result, millis) -> log.at(Level.INFO).log("Found %d %s classes in %d ms", result.size(), description, millis)
        );
    }

    @NotNull
    private Set<? extends Class<?>> scan(@NotNull BiPredicate<String, String> classInfoFilter,
                                         @NotNull Predicate<Class<?>> classFilter) {
        return classPath
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

    public static <T> T timeIt(@NotNull Supplier<T> supplier, @NotNull ObjLongConsumer<T> timeConsumer) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        T result = supplier.get();
        long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        timeConsumer.accept(result, elapsedMillis);
        return result;
    }
}
