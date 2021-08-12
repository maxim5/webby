package io.webby.testing;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.db.kv.StorageType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.function.Consumer;

public class Testing {
    public static final boolean VERBOSE = false;
    public static final boolean READABLE = true;

    public static final String DEFAULT_WEB_PATH = "src/test/resources";
    public static final String DEFAULT_VIEW_PATH = "src/test/resources";

    public static final Charset CHARSET = Charset.defaultCharset();

    @NotNull
    public static Injector testStartupNoHandlers(@NotNull Module... modules) {
        return testStartup(settings -> settings.setInterceptorFilter((pkg, cls) -> false), modules);
    }

    @NotNull
    public static Injector testStartup(@NotNull Class<?> clazz, @NotNull Module... modules) {
        return testStartup(settings -> settings.setHandlerClassOnly(clazz), modules);
    }

    @NotNull
    public static Injector testStartup(@NotNull String packageName, @NotNull Module... modules) {
        return testStartup(settings -> settings.setHandlerPackageOnly(packageName), modules);
    }

    @NotNull
    public static Injector testStartup(@NotNull Consumer<AppSettings> consumer, @NotNull Module... modules) {
        AppSettings settings = new AppSettings();
        settings.setDevMode(true);
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setViewPath(DEFAULT_VIEW_PATH);
        settings.setHandlerFilter((pkg, cls) -> false);
        settings.setStorageType(StorageType.JAVA_MAP);
        consumer.accept(settings);
        return testStartup(settings, modules);
    }

    @NotNull
    public static Injector testStartup(@NotNull AppSettings settings, @NotNull Module... modules) {
        Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, VERBOSE ? Level.TRACE : Level.WARN);

        Locale.setDefault(Locale.US);  // any way to remove this?

        return Webby.initGuice(settings, modules);
    }
}
