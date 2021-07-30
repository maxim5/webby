package io.webby;

import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.db.kv.StorageType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Testing {
    public static final boolean VERBOSE = false;
    public static final boolean READABLE = true;

    public static final String DEFAULT_WEB_PATH = "src/test/resources";
    public static final String DEFAULT_VIEW_PATH = "src/test/resources";

    public static final Charset CHARSET = Charset.defaultCharset();

    @NotNull
    public static Injector testStartupNoHandlers() {
        return testStartup(settings -> settings.setHandlerFilter((pkg, cls) -> false));
    }

    @NotNull
    public static Injector testStartup(@NotNull Class<?> clazz) {
        return testStartup(settings -> settings.setHandlerClassOnly(clazz));
    }

    @NotNull
    public static Injector testStartup(@NotNull String packageName) {
        return testStartup(settings -> settings.setHandlerPackageOnly(packageName));
    }

    @NotNull
    public static Injector testStartup(@NotNull Consumer<AppSettings> consumer) {
        AppSettings settings = new AppSettings();
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setViewPath(DEFAULT_VIEW_PATH);
        settings.setStorageType(StorageType.JAVA_MAP);
        consumer.accept(settings);
        return testStartup(settings);
    }

    @NotNull
    public static Injector testStartup(@NotNull AppSettings settings) {
        Level level = VERBOSE ? Level.ALL : Level.WARNING;
        LogManager.getLogManager().getLogger("").setLevel(level);

        Locale.setDefault(Locale.US);  // any way to remove this?

        return Webby.initGuice(settings);
    }
}
