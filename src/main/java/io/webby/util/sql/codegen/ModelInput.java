package io.webby.util.sql.codegen;

import io.webby.util.sql.arch.Naming;
import org.jetbrains.annotations.NotNull;

public record ModelInput(@NotNull Class<?> modelClass, @NotNull String modelName) {
    public ModelInput(@NotNull Class<?> modelClass) {
        this(modelClass, Naming.generatedSimpleName(modelClass));
    }
}
