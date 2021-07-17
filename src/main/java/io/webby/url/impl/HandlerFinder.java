package io.webby.url.impl;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import io.webby.app.AppSettings;
import io.webby.url.Serve;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HandlerFinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private AppSettings appSettings;

    public Set<? extends Class<?>> getHandlerClasses() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Set<? extends Class<?>> result = getAnnotatedClasses(appSettings.packageTester(), Serve.class);
            long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
            log.at(Level.INFO).log("Found %d handler classes in %d ms", result.size(), elapsedMillis);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to collect handler classes", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @NotNull
    private static Set<? extends Class<?>> getAnnotatedClasses(@NotNull Predicate<String> predicate,
                                                               @NotNull Class<? extends Annotation> annotation)
            throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(classInfo -> predicate.test(classInfo.getPackageName()))
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
