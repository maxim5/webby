package io.webby.testing;

import com.google.inject.Module;
import com.google.inject.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class TestingModules {
    public static <T> @NotNull Module provided(@NotNull Class<T> klass, @NotNull Provider<T> provider) {
        return binder -> binder.bind(klass).toProvider(provider);
    }

    public static <T> @NotNull Module provided(@NotNull Class<T> klass, @NotNull AtomicReference<T> reference) {
        return binder -> binder.bind(klass).toProvider(reference::get);
    }
}
