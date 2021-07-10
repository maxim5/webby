package io.webby.url;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.common.reflect.ClassPath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HandlerFinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final String PACKAGE = "io.webby";

    public Set<? extends Class<?>> getHandlerClasses() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Set<? extends Class<?>> result = getAnnotatedClasses(PACKAGE, Serve.class);
            long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
            log.at(Level.INFO).log("Found %d handlers in %d ms", result.size(), elapsedMillis);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to collect handler classes", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @NotNull
    private static Set<? extends Class<?>> getAnnotatedClasses(@NotNull String packageName,
                                                               @NotNull Class<? extends Annotation> annotation)
            throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(classInfo -> classInfo.getPackageName().startsWith(packageName))
                .map((ClassPath.ClassInfo classInfo) -> {
                    try {
                        return classInfo.load();
                    } catch (NoClassDefFoundError e) {
                        log.at(Level.WARNING).withCause(e)
                                .log("Failed to load class: %s.%s", classInfo.getPackageName(), classInfo.getName());
                        return null;
                    }
                })
                .filter(load -> load != null && load.isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }
}
