package io.webby.orm.arch.factory;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class RunInputs {
    private final List<ModelInput> models;
    private final List<PojoInput> pojos;

    public RunInputs(@NotNull List<ModelInput> models, @NotNull List<PojoInput> pojos) {
        this.models = models;
        this.pojos = pojos;
    }

    public static @NotNull RunInputs of(@NotNull ModelInput @NotNull ... models) {
        return new RunInputs(List.of(models), List.of());
    }

    public @NotNull Optional<ModelInput> findInputByModel(@NotNull Class<?> modelClass) {
        return Streams.stream(models)
            .filter(input -> input.modelClass().equals(modelClass))
            .findFirst();
    }

    public @NotNull Iterable<ModelInput> models() {
        return models;
    }

    public @NotNull Iterable<PojoInput> pojos() {
        return pojos;
    }
}
