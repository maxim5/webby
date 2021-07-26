package io.webby.url.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.url.annotate.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HandlerScanner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    static final Class<? extends Annotation> MAIN_HANDLER_ANNOTATION = Serve.class;
    static final Set<Class<? extends Annotation>>
            HANDLER_CLASS_ANNOTATIONS = Set.of(Json.class, Protobuf.class);
    static final Set<Class<? extends Annotation>>
            HANDLER_METHOD_ANNOTATIONS = Set.of(GET.class, POST.class, PUT.class, DELETE.class, Call.class);

    @Inject private Settings settings;

    // More precisely, handler candidates.
    @NotNull
    public Set<? extends Class<?>> getHandlerClassesFromClasspath() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Set<? extends Class<?>> result = scanHandlerClasses(settings.filter());
            long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
            log.at(Level.INFO).log("Found %d handler classes in %d ms", result.size(), elapsedMillis);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to collect handler classes", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @NotNull
    private static Set<? extends Class<?>> scanHandlerClasses(@NotNull BiPredicate<String, String> filter)
            throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(classInfo -> filter.test(classInfo.getPackageName(), classInfo.getSimpleName()))
                .map((ClassPath.ClassInfo classInfo) -> {
                    try {
                        return classInfo.load();
                    } catch (NoClassDefFoundError e) {
                        log.at(Level.WARNING).withCause(e)
                                .log("Failed to load class: %s.%s", classInfo.getPackageName(), classInfo.getName());
                        return null;
                    }
                })
                .filter(klass -> klass != null && isHandlerClass(klass, MAIN_HANDLER_ANNOTATION, HANDLER_CLASS_ANNOTATIONS, HANDLER_METHOD_ANNOTATIONS))
                .collect(Collectors.toSet());
    }

    // Note: if the class doesn't have any methods, it will be simply ignored down the line.
    // Here a quick check will suffice.
    @VisibleForTesting
    static boolean isHandlerClass(@NotNull Class<?> klass,
                                  @NotNull Class<? extends Annotation> main,
                                  @NotNull Set<Class<? extends Annotation>> onClass,
                                  @NotNull Set<Class<? extends Annotation>> onMethod) {
        return klass.isAnnotationPresent(main) ||
                matchesAny(klass, onClass) ||
                Arrays.stream(klass.getDeclaredMethods()).anyMatch(method -> matchesAny(method, onMethod));
    }

    @VisibleForTesting
    static boolean matchesAny(@NotNull AnnotatedElement element, @NotNull Set<Class<? extends Annotation>> annotations) {
        return Arrays.stream(element.getAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(annotations::contains);
    }
}
