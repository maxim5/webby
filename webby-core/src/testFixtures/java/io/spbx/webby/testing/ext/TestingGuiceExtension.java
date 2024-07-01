package io.spbx.webby.testing.ext;

import io.spbx.webby.app.AppSettings;
import io.spbx.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

// Makes sure that the properties are initialized in fast tests.
public class TestingGuiceExtension implements BeforeAllCallback {
    private AppSettings settings;

    private TestingGuiceExtension() {}

    public static @NotNull TestingGuiceExtension lite() {
        return new TestingGuiceExtension();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        settings = Testing.setupLite();
    }

    public @NotNull AppSettings settings() {
        return settings;
    }
}
