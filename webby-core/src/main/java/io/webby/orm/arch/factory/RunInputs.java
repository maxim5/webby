package io.webby.orm.arch.factory;

import com.google.common.collect.Streams;
import io.webby.orm.codegen.ModelInput;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class RunInputs implements Iterable<ModelInput> {
    private final List<ModelInput> inputs;

    RunInputs(@NotNull List<ModelInput> inputs) {
        this.inputs = inputs;
    }

    public static @NotNull RunInputs of(@NotNull ModelInput @NotNull ... inputs) {
        return new RunInputs(List.of(inputs));
    }

    public @NotNull Optional<ModelInput> findInputByModel(@NotNull Class<?> modelClass) {
        return Streams.stream(inputs)
            .filter(input -> input.modelClass().equals(modelClass))
            .findFirst();
    }

    @Override
    public @NotNull Iterator<ModelInput> iterator() {
        return inputs.iterator();
    }
}
