package io.webby.util.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class EasyAnnotations {
    public static <A extends Annotation> @Nullable A getAnnotationOrNull(@NotNull Class<?> klass,
                                                                         @NotNull Class<A> annotation) {
        return klass.isAnnotationPresent(annotation) ? klass.getAnnotation(annotation) : null;
    }

    public static <A extends Annotation> @NotNull Optional<A> getOptionalAnnotation(@NotNull Class<?> klass,
                                                                                    @NotNull Class<A> annotation) {
        return Optional.ofNullable(getAnnotationOrNull(klass, annotation));
    }
}
