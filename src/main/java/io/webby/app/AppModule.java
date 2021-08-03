package io.webby.app;

import com.google.inject.AbstractModule;
import io.webby.common.Lifetime;
import org.jetbrains.annotations.NotNull;

public class AppModule extends AbstractModule {
    private final AppSettings settings;

    public AppModule(@NotNull AppSettings settings) {
        this.settings = settings;
    }

    public void configure() {
        bind(AppSettings.class).toInstance(settings);
        bind(Settings.class).toInstance(settings);
        bind(AppLifetime.class).asEagerSingleton();
        bind(AppMaintenance.class).asEagerSingleton();
        bind(Lifetime.class).toProvider(AppLifetime.class);
    }
}
