package io.webby.orm.arch;

import io.webby.orm.codegen.ModelInput;
import io.webby.orm.testing.FakeModelAdaptersScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ArchTesting {
    public static @NotNull RunContext newRunContext(@NotNull Class<?> ... models) {
        return new RunContext(newRunInputs(models), new FakeModelAdaptersScanner());
    }

    private static @NotNull RunInputs newRunInputs(@NotNull Class<?> ... models) {
        List<ModelInput> modelInputs = Arrays.stream(models).map(ModelInput::of).toList();
        return new RunInputs(modelInputs);
    }
}
