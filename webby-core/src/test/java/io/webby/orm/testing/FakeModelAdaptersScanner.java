package io.webby.orm.testing;

import io.webby.orm.codegen.ModelAdaptersScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FakeModelAdaptersScanner extends ModelAdaptersScanner {
    public FakeModelAdaptersScanner(@NotNull Map<Class<?>, Class<?>> byClass, @NotNull Map<String, Class<?>> bySimpleName) {
        super(byClass, bySimpleName);
    }

    public FakeModelAdaptersScanner() {
        this(Map.of(), Map.of());
    }
}
