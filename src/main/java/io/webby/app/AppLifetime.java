package io.webby.app;

import com.google.inject.Provider;
import io.webby.common.Lifetime;
import org.jetbrains.annotations.NotNull;

public class AppLifetime implements Provider<Lifetime> {
    private final Lifetime.Definition lifetime = new Lifetime.Definition();

    @NotNull
    public Lifetime.Definition getLifetime() {
        return lifetime;
    }

    @Override
    public Lifetime get() {
        return lifetime;
    }
}
