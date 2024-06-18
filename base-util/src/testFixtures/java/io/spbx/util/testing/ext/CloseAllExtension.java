package io.spbx.util.testing.ext;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

public class CloseAllExtension implements AfterEachCallback {
    private final List<AutoCloseable> closeablesPerTest = new ArrayList<>();

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        for (AutoCloseable closeable : closeablesPerTest) {
            closeable.close();
        }
    }

    public void addCloseable(@NotNull AutoCloseable closeable) {
        closeablesPerTest.add(closeable);
    }
}
