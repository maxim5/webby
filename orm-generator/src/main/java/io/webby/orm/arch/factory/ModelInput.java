package io.webby.orm.arch.factory;

import io.spbx.orm.api.annotate.Model;
import io.spbx.util.base.EasyStrings;
import io.spbx.util.reflect.EasyAnnotations;
import io.webby.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.spbx.util.base.EasyStrings.ofNonEmpty;
import static io.webby.orm.arch.model.JavaNameValidator.validateJavaIdentifier;
import static io.webby.orm.arch.model.SqlNameValidator.validateSqlName;

public record ModelInput(@NotNull Class<?> modelClass,
                         @Nullable Class<?> modelInterface,
                         @NotNull String javaModelName,
                         @NotNull String sqlName,
                         @NotNull String javaTableName) {
    public ModelInput {
        validateJavaIdentifier(javaModelName);
        validateSqlName(sqlName);
        validateJavaIdentifier(javaTableName);
    }

    public static @NotNull ModelInput of(@NotNull Class<?> modelClass) {
        Model annotation = EasyAnnotations.getAnnotationOrNull(modelClass, Model.class);
        if (annotation != null) {
            Class<?> exposeAs = annotation.exposeAs();
            String javaModelName = EasyStrings.ofNonEmpty(annotation.javaName())
                .orElseGet(() -> Naming.generatedSimpleJavaName(isSet(exposeAs) ? exposeAs : modelClass));
            String sqlName = EasyStrings.ofNonEmpty(annotation.sqlName())
                .orElseGet(() -> Naming.modelSqlName(javaModelName));
            String javaTableName = ofNonEmpty(annotation.javaTableName())
                .orElseGet(() -> Naming.generatedJavaTableName(javaModelName));
            return new ModelInput(modelClass, exposeAs, javaModelName, sqlName, javaTableName);
        }
        String javaModelName = Naming.generatedSimpleJavaName(modelClass);
        String sqlName = Naming.modelSqlName(javaModelName);
        String javaTableName = Naming.generatedJavaTableName(javaModelName);
        return new ModelInput(modelClass, null, javaModelName, sqlName, javaTableName);
    }

    public @NotNull Iterable<Class<?>> keys() {
        return isSet(modelInterface) ? List.of(modelClass, modelInterface) : List.of(modelClass);
    }

    private static boolean isSet(@Nullable Class<?> klass) {
        return klass != null && klass != Void.class;
    }
}
