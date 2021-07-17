package io.webby.app;

import com.google.inject.AbstractModule;
import org.jetbrains.annotations.NotNull;

public class AppModule extends AbstractModule {
    private final AppSettings settings;

    public AppModule(@NotNull AppSettings settings) {
        this.settings = settings;
    }

    public void configure() {
        bind(Settings.class).toInstance(settings);
        bind(AppSettings.class).toInstance(settings);
    }
}
