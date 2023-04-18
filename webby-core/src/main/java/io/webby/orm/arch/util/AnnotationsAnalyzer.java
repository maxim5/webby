package io.webby.orm.arch.util;

import com.google.common.collect.ImmutableList;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.codegen.ModelInput;
import io.webby.util.base.EasyStrings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static io.webby.orm.arch.InvalidSqlModelException.failIf;
import static io.webby.util.reflect.EasyAnnotations.getOptionalAnnotation;

public class AnnotationsAnalyzer {
    private static final ImmutableList<Class<? extends Annotation>> NULLABLE_ANNOTATIONS = ImmutableList.of(
        javax.annotation.Nullable.class,
        org.checkerframework.checker.nullness.qual.Nullable.class,
        Nullable.class  // retention policy: CLASS
    );
    private static final ImmutableList<Class<?>> NULLABLE_TYPES = ImmutableList.of(Optional.class, AtomicReference.class);

    public static @NotNull Optional<String> getSqlName(@NotNull AnnotatedElement elem) {
        return getOptionalAnnotation(elem, Sql.class).map(Sql::value).flatMap(EasyStrings::ofNonEmpty);
    }

    public static boolean isPrimaryKeyField(@NotNull Field field, @NotNull ModelInput input) {
        String fieldName = field.getName();
        return fieldName.equals("id") ||
            fieldName.equals(Naming.idJavaName(input.javaModelName())) ||
            fieldName.equals(Naming.idJavaName(input.modelClass())) ||
            (input.modelInterface() != null && fieldName.equals(Naming.idJavaName(input.modelInterface()))) ||
            merge(getOptionalAnnotation(field, Sql.class).map(Sql::primary), getOptionalAnnotation(field, Sql.PK.class));
    }

    public static boolean isUniqueField(@NotNull AnnotatedElement elem) {
        return merge(getOptionalAnnotation(elem, Sql.class).map(Sql::unique), getOptionalAnnotation(elem, Sql.Unique.class));
    }

    public static boolean isNullableField(@NotNull Field field) {
        for (Class<? extends Annotation> annotation : NULLABLE_ANNOTATIONS) {
            if (getOptionalAnnotation(field, annotation).isPresent()) {
                return true;
            }
        }
        if (NULLABLE_TYPES.contains(field.getType())) {
            return true;
        }
        return merge(getOptionalAnnotation(field, Sql.class).map(Sql::nullable), getOptionalAnnotation(field, Sql.Null.class));
    }

    public static @Nullable String @Nullable [] getDefaults(@NotNull AnnotatedElement elem) {
        Optional<String[]> defaults1 = getOptionalAnnotation(elem, Sql.class).map(Sql::defaults);
        Optional<String[]> defaults2 = getOptionalAnnotation(elem, Sql.Default.class).map(Sql.Default::value);
        failIf(defaults1.isPresent() && defaults2.isPresent(), "Element contains ambiguous annotations: %s", elem);
        return defaults1.or(() -> defaults2).orElse(null);
    }

    public static @Nullable Class<?> getViaClass(@NotNull AnnotatedElement elem) {
        Optional<Class<?>> via1 = getOptionalAnnotation(elem, Sql.class).map(Sql::via);
        Optional<Class<?>> via2 = getOptionalAnnotation(elem, Sql.Via.class).map(Sql.Via::value);
        failIf(via1.isPresent() && via2.isPresent(), "Element contains ambiguous annotations: %s", elem);
        return via1.or(() -> via2).filter(klass -> klass != Void.class).orElse(null);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static boolean merge(@NotNull Optional<Boolean> first, @NotNull Optional<?> second) {
        return first.orElseGet(second::isPresent);
    }
}
