package io.webby.util.sql.testing;

import io.webby.util.sql.codegen.FQN;
import io.webby.util.sql.codegen.ModelAdaptersScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeModelAdaptersScanner implements ModelAdaptersScanner {
    @Override
    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model) {
        return null;
    }

    @Override
    public @NotNull FQN locateAdapterFqn(@NotNull Class<?> model) {
        throw new UnsupportedOperationException("Not implemented. Model: " + model);
    }
}
