package io.webby.url.impl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.url.annotate.*;
import io.spbx.util.classpath.ClasspathScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Set;

public class HandlerScanner {
    static final Class<? extends Annotation> MAIN_HANDLER_ANNOTATION = Serve.class;
    static final Set<Class<? extends Annotation>>
            HANDLER_CLASS_ANNOTATIONS = Set.of(Json.class, Protobuf.class);
    static final Set<Class<? extends Annotation>>
            HANDLER_METHOD_ANNOTATIONS = Set.of(GET.class, POST.class, PUT.class, DELETE.class, Call.class);

    @Inject private Settings settings;
    @Inject private ClasspathScanner scanner;

    // More precisely, handler candidates.
    public @NotNull Set<Class<?>> getHandlerClassesFromClasspath() {
        return scanner.timed("handler").scanToSet(
            settings.handlerFilter(),
            klass -> isHandlerClass(klass, MAIN_HANDLER_ANNOTATION, HANDLER_CLASS_ANNOTATIONS, HANDLER_METHOD_ANNOTATIONS)
        );
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
