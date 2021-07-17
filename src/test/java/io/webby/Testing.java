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

    @NotNull
    public static Injector testStartup() {
        return testStartup(Testing.class);
    }

    @NotNull
    public static Injector testStartup(Class<?> clazz) {
        Level level = VERBOSE ? Level.ALL : Level.WARNING;
        LogManager.getLogManager().getLogger("").setLevel(level);

        Locale.setDefault(Locale.US);  // any way to remove this?

        AppSettings settings = new AppSettings();
        settings.setWebPath("src/test/resources");
        settings.setPackage(clazz.getPackageName());
        return Guice.createInjector(new AppModule(settings), new NettyModule(), new UrlModule());
    }
}
