package io.webby;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.webby.app.AppModule;
import io.webby.app.AppSettings;
import io.webby.netty.NettyModule;
import io.webby.url.UrlModule;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Testing {
    public static final boolean VERBOSE = false;
    public static final boolean READABLE = true;

    public static final String DEFAULT_WEB_PATH = "src/test/resources";

    @NotNull
    public static Injector testStartupNoHandlers() {
        AppSettings settings = new AppSettings();
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setFilter((pkg, cls) -> false);
        return testStartup(settings);
    }

    @NotNull
    public static Injector testStartup(@NotNull Class<?> clazz) {
        AppSettings settings = new AppSettings();
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setClassOnly(clazz);
        return testStartup(settings);
    }

    @NotNull
    public static Injector testStartup(@NotNull String packageName) {
        AppSettings settings = new AppSettings();
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setPackageOnly(packageName);
        return testStartup(settings);
    }

    @NotNull
    public static Injector testStartup(@NotNull AppSettings settings) {
        Level level = VERBOSE ? Level.ALL : Level.WARNING;
        LogManager.getLogManager().getLogger("").setLevel(level);

        Locale.setDefault(Locale.US);  // any way to remove this?

        return Guice.createInjector(new AppModule(settings), new NettyModule(), new UrlModule());
    }
}
