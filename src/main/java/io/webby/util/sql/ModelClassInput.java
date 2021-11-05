package io.webby.util.sql;

import io.webby.util.sql.schema.Naming;
import org.jetbrains.annotations.NotNull;

public record ModelClassInput(@NotNull Class<?> modelClass, @NotNull String modelName) {
    public ModelClassInput(@NotNull Class<?> modelClass) {
        this(modelClass, Naming.generatedSimpleName(modelClass));
    }
}
