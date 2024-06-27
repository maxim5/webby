package io.spbx.webby.app;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import io.spbx.util.classpath.ClasspathScanner;
import io.spbx.util.props.PropertyMap;
import io.spbx.webby.common.Lifetime;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public class AppModule extends AbstractModule {
    private final AppSettings settings;

    public AppModule(@NotNull AppSettings settings) {
        this.settings = settings;
    }

    public void configure() {
        bind(AppSettings.class).toInstance(settings);
        bind(Settings.class).toInstance(settings);
        bind(Charset.class).toInstance(settings.charset());
        bind(PropertyMap.class).toProvider(AppSettings::live);
        bind(AppLifetime.class).asEagerSingleton();
        bind(AppMaintenance.class).asEagerSingleton();
        bind(Lifetime.class).toProvider(AppLifetime.class);
        bind(AppClasspathScanner.class).asEagerSingleton();
        bind(ClasspathScanner.class).to(AppClasspathScanner.class).asEagerSingleton();

        bind(EventBus.class).asEagerSingleton();
    }
}
