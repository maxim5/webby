package io.webby.orm.codegen;

import io.webby.orm.arch.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ModelInput(@NotNull Class<?> modelClass, @Nullable Class<?> modelInterface, @NotNull String modelName) {
    public ModelInput(@NotNull Class<?> modelClass, @NotNull Class<?> modelInterface) {
        this(modelClass, modelInterface, Naming.generatedSimpleJavaName(modelInterface));
    }

    public ModelInput(@NotNull Class<?> modelClass) {
        this(modelClass, null, Naming.generatedSimpleJavaName(modelClass));
    }

    public @NotNull Iterable<Class<?>> keys() {
        return modelInterface != null ? List.of(modelClass, modelInterface) : List.of(modelClass);
    }
}
