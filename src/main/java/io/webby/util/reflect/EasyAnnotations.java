package io.webby.util.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public class EasyAnnotations {
    public static <A extends Annotation> @Nullable A getAnnotationOrNull(@NotNull AnnotatedElement element,
                                                                         @NotNull Class<A> annotation) {
        return element.isAnnotationPresent(annotation) ? element.getAnnotation(annotation) : null;
    }

    public static <A extends Annotation> @NotNull Optional<A> getOptionalAnnotation(@NotNull AnnotatedElement element,
                                                                                    @NotNull Class<A> annotation) {
        return Optional.ofNullable(getAnnotationOrNull(element, annotation));
    }
}
