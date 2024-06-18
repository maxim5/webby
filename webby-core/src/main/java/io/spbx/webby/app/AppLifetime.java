package io.spbx.webby.app;

import com.google.inject.Provider;
import io.spbx.webby.common.Lifetime;
import org.jetbrains.annotations.NotNull;

public class AppLifetime implements Provider<Lifetime> {
    private final Lifetime.Definition lifetime = new Lifetime.Definition();

    public @NotNull Lifetime.Definition getLifetime() {
        return lifetime;
    }

    @Override
    public Lifetime get() {
        return lifetime;
    }
}
