package io.webby.testing;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.webby.Webby;
import io.webby.app.AppLifetime;
import io.webby.app.AppSettings;
import io.webby.netty.marshal.Json;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Testing {
    public static final boolean LOG_VERBOSE = false;

    public static final String DEFAULT_WEB_PATH = "src/test/resources";
    public static final String DEFAULT_VIEW_PATH = "src/test/resources";

    public static @NotNull Injector testStartup(@NotNull Module... modules) {
        return testStartup(settings -> {}, modules);
    }

    public static @NotNull Injector testStartup(@NotNull Consumer<AppSettings> consumer) {
        return testStartup(consumer, new Module[0]);
    }

    public static @NotNull Injector testStartup(@NotNull Consumer<AppSettings> consumer, @NotNull Module... modules) {
        AppSettings settings = new AppSettings();
        settings.setDevMode(true);
        settings.setCharset(TestingBytes.CHARSET);
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setViewPath(DEFAULT_VIEW_PATH);
        settings.modelFilter().setPredicateUnsafe((pkg, cls) -> false);
        settings.handlerFilter().setPredicateUnsafe((pkg, cls) -> false);
        settings.interceptorFilter().setPredicateUnsafe((pkg, cls) -> false);
        settings.storageSettings()
                .disableKeyValueStorage()
                .disableSqlStorage();
        consumer.accept(settings);
        return testStartup(settings, modules);
    }

    public static @NotNull Injector testStartup(@NotNull AppSettings settings, @NotNull Module... modules) {
        Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, LOG_VERBOSE ? Level.TRACE : Level.WARN);

        Locale.setDefault(Locale.US);  // any way to remove this?

        return Internals.injector = Webby.initGuice(settings, modules);
    }

    public static class Internals {
        private static Injector injector;

        public static <T> @NotNull T getInstance(@NotNull Class<T> type) {
            assertNotNull(injector, "Test Guice Injector is not initialized");
            return injector.getInstance(type);
        }

        public static <T> @Nullable T getInstanceOrNull(@NotNull Class<T> type) {
            return injector != null ? getInstance(type) : null;
        }

        public static @NotNull Charset charset() {
            return injector != null ? getInstance(Charset.class) : Charset.defaultCharset();
        }

        public static @NotNull AppSettings settings() {
            return getInstance(AppSettings.class);
        }

        public static @NotNull Json json() {
            // An alternative that always works:
            // new GsonMarshaller(new Gson(), TestingBytes.CHARSET)
            return getInstance(Json.class);
        }

        public static void terminate() {
            if (injector != null) {
                injector.getInstance(AppLifetime.class).getLifetime().terminate();
            }
        }
    }
}
