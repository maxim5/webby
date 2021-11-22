package io.webby.util.sql.codegen;

import io.webby.util.sql.arch.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ModelInput(@NotNull Class<?> modelClass, @Nullable Class<?> modelInterface, @NotNull String modelName) {
    public ModelInput(@NotNull Class<?> modelClass, @NotNull Class<?> modelInterface) {
        this(modelClass, modelInterface, Naming.generatedSimpleName(modelInterface));
    }

    public ModelInput(@NotNull Class<?> modelClass) {
        this(modelClass, null, Naming.generatedSimpleName(modelClass));
    }

    public @NotNull Iterable<Class<?>> keys() {
        return modelInterface != null ? List.of(modelClass, modelInterface) : List.of(modelClass);
    }
}
