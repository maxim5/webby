package io.spbx.orm.arch.factory;

import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.codegen.ModelAdaptersLocator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

// This exists only due to unresolved dependency on `TestingArch` fixture.
public class IntegrationTestingArch {
    public static @NotNull TableArch buildTableArch(@NotNull Class<?> model, @NotNull ModelAdaptersLocator locator) {
        RunInputs inputs = new RunInputs(Stream.of(model).map(ModelInput::of).toList(), List.of());
        RunResult runResult = new ArchFactory(locator).build(inputs);
        return runResult.getTableOrDie(model);
    }
}
